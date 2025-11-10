package uz.pdp.water_delivery.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.dto.CourierDTO;
import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.entity.*;
import uz.pdp.water_delivery.entity.enums.CourierStatus;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.exception.DuplicatePhoneNumberException;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.services.service.DeleteMessageService;
import uz.pdp.water_delivery.services.serviceImple.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CourierController {

    private final CourierRepository courierRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final DeliveryTimeRepository deliveryTimeRepository;
    private final DistrictRepository districtRepository;
    private final LogErrorFile logErrorFile;
    private final TelegramUserRepository telegramUserRepository;
    private final TelegramBot telegramBot;
    private final BotUtils botUtils;
    private final DeleteMessageService deleteMessageService;
    private final BottleTypesRepository bottleTypesRepository;

    @GetMapping("/admin/couriers")
    public String couriers(Model model) {
        List<Courier> couriers = courierRepository.findAll();
        List<CourierDTO> courierDTOs = new ArrayList<>();
        for (Courier courier : couriers) {
            boolean hasOrders = orderRepository.existsByCourier(courier);
            courierDTOs.add(new CourierDTO(courier, courier.getDistricts(), hasOrders));
        }
        model.addAttribute("couriers", courierDTOs);
        return "admin/couriers";
    }

    @GetMapping("/admin/couriers/new")
    public String createCourierForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "admin/courier-form";
    }

    @Transactional
    @PostMapping("/admin/couriers/save")
    public String saveCourier(@ModelAttribute("userDTO") UserDTO userDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Formda xatoliklar mavjud. Iltimos, tekshirib qayta kiriting.");
            return "admin/courier-form";
        }
        try {
            String repairedPhone = PhoneRepairUtil.repair(userDTO.getPhone());
            Optional<User> existingUser = userRepository.findByPhone(repairedPhone);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                Optional<Courier> existingCourier = courierRepository.findByUser(user);
                if (existingCourier.isPresent()) {
                    model.addAttribute("errorMessage", "Bunday telefon raqami bilan Courier mavjud.");
                    return "admin/courier-form";
                }
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
            } else {
                user = new User();
                user.setPhone(repairedPhone);
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setPassword(null);
                userRepository.save(user);
            }

            Role deliveryRole = roleRepository.findByRoleName(RoleName.ROLE_DELIVERY);
            if (deliveryRole == null) {
                throw new RuntimeException("ROLE_DELIVERY roli topilmadi.");
            }
            if (user.getRoles().isEmpty()) {
                user.setRoles(List.of(deliveryRole));
            }else if(user.getRoles().contains(deliveryRole)){
                user.getRoles().add(deliveryRole);
            }

            Courier courier = Courier.builder()
                    .courierStatus(CourierStatus.WAITING)
                    .carNumber(userDTO.getCarNumber())
                    .carType(userDTO.getCarType())
                    .districts(userDTO.getDistricts())
                    .isActive(userDTO.getCourierStatus().equals("ACTIVE"))
                    .user(user)
                    .build();
            courierRepository.save(courier);

            notifyCourierAccepted(user);

            model.addAttribute("successMessage", "Kuryer muvaffaqiyatli saqlandi.");
            return "redirect:/admin/couriers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Kuryerni saqlashda xatolik yuz berdi.");
            logErrorFile.logError(e, "saveCourier", null);
            return "admin/courier-form";
        }
    }

    private void notifyCourierAccepted(User user) {
        TelegramUser telegramUser = telegramUserRepository.findByUser(user).orElse(null);
        if (telegramUser != null) {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    "✅ Siz kuryerlikga qabul qilindizgiz. Ishni boshlash uchun \n ↙️ Menudan \"Qayta boshlash 🔄\" buyrug'ini yuboring."
            );
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Siz kuryerlikga qabul qilindizgiz. Ishni boshlash uchun Menudan \"Qayta boshlash\" buyrug'ini yuboring.");
        }
    }

    @GetMapping("/admin/couriers/edit/{id}")
    public String editCourierForm(@PathVariable Long id, Model model) {
        Courier courier = courierRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid courier Id:" + id));
        List<District> districts = districtRepository.getAllDistricts();
        model.addAttribute("courier", courier);
        model.addAttribute("districts", districts);
        return "admin/courier-edit";
    }

    @Transactional
    @PostMapping("/admin/couriers/update/{id}")
    public String updateCourier(@PathVariable Long id, @ModelAttribute Courier courier, Model model) {
        try {
            Courier existingCourier = courierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Noto'g'ri kuryer ID:" + id));
            String repairedPhone = PhoneRepairUtil.repair(courier.getUser().getPhone());

            boolean phoneExistsForAnotherCourier = courierRepository.existsByUserPhoneAndIdNot(repairedPhone, id);
            if (phoneExistsForAnotherCourier) {
                model.addAttribute("errorMessage", "Ushbu telefon raqami boshqa kuryer tomonidan foydalanilmoqda.");
                return "admin/courier-edit";
            }

            boolean hasAssignedOrders = orderRepository.existsByCourierAndOrderStatus(existingCourier, OrderStatus.ASSIGNED);
            if (hasAssignedOrders && !existingCourier.getIsActive().equals(courier.getIsActive())) {
                model.addAttribute("errorMessage", "Kuryer faol buyurtmalarni boshqarayotganligi sababli faoliyat holatini o'zgartirib bo'lmaydi.");
                return "admin/courier-edit";
            }

            existingCourier.getUser().setFirstName(courier.getUser().getFirstName());
            existingCourier.getUser().setLastName(courier.getUser().getLastName());
            existingCourier.getUser().setPhone(repairedPhone);
            existingCourier.setCarType(courier.getCarType());
            existingCourier.setCarNumber(courier.getCarNumber());
            existingCourier.setIsActive(courier.getIsActive());
            existingCourier.setDistricts(courier.getDistricts());
            courierRepository.save(existingCourier);
        } catch (Exception e) {
            logErrorFile.logError(e, "updateCourier", 0L);
        }

        return "redirect:/admin/couriers";
    }

    @Transactional
    @GetMapping("/admin/couriers/delete/{id}")
    public String deleteCourier(@PathVariable Long id, Model model) {
        try {
            Courier courier = courierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid courier ID: " + id));
            boolean hasOrders = orderRepository.existsByCourierAndOrderStatus(courier, OrderStatus.ASSIGNED);
            if (hasOrders) {
                model.addAttribute("errorMessage", "Kuryerni o'chirib bo'lmaydi, chunki u bilan bog'liq buyurtmalar mavjud.");
                return "redirect:/admin/couriers";
            }
            if (!hasOrders) {
                TelegramUser telegramUser = telegramUserRepository.findByUser(courier.getUser()).orElse(null);
                hasOrders = orderRepository.existsByTelegramUser(telegramUser);
                if (hasOrders) {
                    model.addAttribute("errorMessage", "Kuryerni o'chirib bo'lmaydi, chunki u bilan bog'liq buyurtmalar mavjud.");
                    return "redirect:/admin/couriers";
                }
                if (telegramUser != null) {
                    telegramUserRepository.delete(telegramUser);
                }
                courierRepository.delete(courier);
                userRepository.delete(courier.getUser());
                model.addAttribute("successMessage", "Kuryer muvaffaqiyatli o'chirildi.");
            }
            return "redirect:/admin/couriers";
        } catch (Exception e) {
            logErrorFile.logError(e, "deleteCourier", null);
        }
        return "redirect:/admin/couriers";
    }


}

