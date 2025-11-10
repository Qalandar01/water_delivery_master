package uz.pdp.water_delivery.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.dto.BottleTypeDTO;
import uz.pdp.water_delivery.entity.BottleTypes;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {


    private final BottleTypesRepository bottleTypesRepository;
    private final OrderProductRepository orderProductRepository;
    private final LogErrorFile logErrorFile;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    @GetMapping("/admin")
    public String admin(Model model) {
        List<User> users = userRepository.findAllByRolesRoleName(RoleName.ROLE_OPERATOR);
        model.addAttribute("users", users);
        return "admin/admin";
    }


    @GetMapping("/admin/change-gift-water")
    public String changeGiftWater(Model model) {
        getBottleTypeIsTrue(model);
        return "admin/chegirmalar";
    }

    private void getBottleTypeIsTrue(Model model) {
        List<BottleTypes> bottleTypes = bottleTypesRepository.findAllByActiveTrue();
        for (BottleTypes bottleType : bottleTypes) {
            long orderCount = orderProductRepository.countByBottleTypes(bottleType);
            bottleType.setOrderCount(orderCount);
            bottleTypesRepository.save(bottleType);
        }
        model.addAttribute("bottleTypes", bottleTypes);
    }

    @PostMapping("/admin/change-gift-water")
    public String changeGiftWater(
            @RequestParam("bottleTypeId") Integer bottleTypeId,
            @RequestParam("sale_amount") Integer saleAmount,
            @RequestParam("sale_discount") Integer saleDiscount,
            @RequestParam(value = "sale_active", required = false) Boolean saleActive,
            @RequestParam("sale_start_time") LocalDate saleStartTime,
            @RequestParam("sale_end_time") LocalDate saleEndTime,
            Model model) {

        if (saleActive == null) {
            saleActive = false;
        }

        boolean isValidDateRange = saleStartTime.isBefore(saleEndTime);
        if (!isValidDateRange) {
            model.addAttribute("errorMessage", "Tugash sanasi boshlanish sanasidan kichik bo'lishi kerak.");
            return "redirect:/admin/change-gift-water";
        }

        boolean isValidDiscount = saleDiscount <= 100 && saleDiscount >= 0;
        if (!isValidDiscount) {
            model.addAttribute("errorMessage", "Chegirma miqdori 0 va 100 orasida bo'lishi kerak.");
            return "redirect:/admin/change-gift-water";
        }

        if (!saleActive) {
            saleStartTime = null;
            saleEndTime = null;
        }

        try {
            BottleTypes bottleType = bottleTypesRepository.findById(bottleTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!"));

            bottleType.setSale_amount(saleAmount);
            bottleType.setSale_discount(saleDiscount);
            bottleType.setSale_active(saleActive);
            bottleType.setSale_startDate(saleStartTime);
            bottleType.setSale_endDate(saleEndTime);

            bottleTypesRepository.save(bottleType);

            model.addAttribute("successMessage", "Chegirma muvaffaqiyatli saqlandi.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
        }
        getBottleTypeIsTrue(model);
        return "admin/chegirmalar";
    }


    /**
     * Chegirma o'chirish
     */
    @GetMapping("/admin/delete/{id}")
    public String deleteDiscount(@PathVariable("id") Integer id, Model model) {
        try {
            BottleTypes bottleType = bottleTypesRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bunday ID ga ega butilka topilmadi!"));

            bottleType.setSale_amount(null);
            bottleType.setSale_discount(null);
            bottleType.setSale_active(false);
            bottleType.setSale_startDate(null);
            bottleType.setSale_endDate(null);

            bottleTypesRepository.save(bottleType);

            model.addAttribute("successMessage", "Chegirma muvaffaqiyatli o'chirildi.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
        }

        return "redirect:/admin/change-gift-water";
    }

    @GetMapping("/admin/add/bottle")
    public String addBottle(Model model) {
        model.addAttribute("bottleTypeDTO", new BottleTypeDTO());
        return "admin/bottle/add-bottle-type";
    }

    @Transactional
    @PostMapping("/admin/add/bottle")
    public String addBottle(@ModelAttribute BottleTypeDTO bottleTypeDTO, Model model) {
        try {
            BottleTypes bottleTypes = new BottleTypes();
            bottleTypes.setType(bottleTypeDTO.getType().trim());
            bottleTypes.setPrice(bottleTypeDTO.getPrice());
            bottleTypes.setActive(bottleTypeDTO.isActive());
            bottleTypes.setDescription(bottleTypeDTO.getDescription());
            bottleTypes.setReturnable(bottleTypeDTO.isReturnable());

            MultipartFile file = bottleTypeDTO.getImage();
            if (!file.isEmpty()) {
                byte[] imageBytes = file.getBytes();
                bottleTypes.setImage(imageBytes);
            }
            if (bottleTypesRepository.existsByType(bottleTypes.getType())) {
                model.addAttribute("errorMessage", "Bunday idish turi mavjud!");
                return "admin/bottle/add-bottle-type";
            }
            bottleTypesRepository.save(bottleTypes);
            model.addAttribute("successMessage", "Idish turi qo'shildi");
            return "redirect:/admin/bottle/menu";
        } catch (Exception e) {
            logErrorFile.logError(e, "addBottle", null);
            model.addAttribute("errorMessage", "Ma'lumotni saqlashda yoki faylni yuklashda xatolik yuz berdi!");
            return "admin/bottle/add-bottle-type";
        }
    }

    @GetMapping("/admin/bottle/menu")
    public String bottleMenu(Model model) {
        getBottleTypeIsTrue(model);
        return "admin/bottle/bottle-menu";
    }


    @GetMapping("/admin/bottle/edit/{id}")
    public String editBottle(@PathVariable Integer id, Model model) {
        BottleTypes bottleType = bottleTypesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));
        String base64Image = Base64.getEncoder().encodeToString(bottleType.getImage());

        BottleTypeDTO bottleTypeDTO = new BottleTypeDTO();
        bottleTypeDTO.setId(bottleType.getId());
        bottleTypeDTO.setType(bottleType.getType());
        bottleTypeDTO.setDescription(bottleType.getDescription());
        bottleTypeDTO.setPrice(bottleType.getPrice());
        bottleTypeDTO.setActive(bottleType.isActive());
        bottleTypeDTO.setReturnable(bottleType.isReturnable());
        model.addAttribute("base64Image", base64Image);
        model.addAttribute("bottleType", bottleTypeDTO);
        return "admin/bottle/bottle-edit";
    }

    @Transactional
    @PostMapping("/admin/bottle/update")
    public String updateBottle(@ModelAttribute BottleTypeDTO bottleTypeDTO) throws IOException {
        try {
            BottleTypes bottleType = bottleTypesRepository.findById(bottleTypeDTO.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Bottle not found"));

            bottleType.setType(bottleTypeDTO.getType());
            bottleType.setDescription(bottleTypeDTO.getDescription());
            bottleType.setPrice(bottleTypeDTO.getPrice());
            bottleType.setActive(bottleTypeDTO.isActive());
            bottleType.setReturnable(bottleTypeDTO.isReturnable());
            MultipartFile image = bottleTypeDTO.getImage();
            if (image != null && !image.isEmpty()) {
                bottleType.setImage(image.getBytes());
            }
            bottleTypesRepository.save(bottleType);
            return "redirect:/admin/bottle/menu";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateBottle", null);
        }
        return "redirect:/admin/bottle/menu";
    }

    @GetMapping("/admin/bottle/delete/{id}")
    public String deleteBottle(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (!bottleTypesRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bottle not found.");
            return "redirect:/operator/bottle/menu";
        }

        try {
            bottleTypesRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete bottle type as it is associated with existing orders.");
            logErrorFile.logError(ex, "deleteBottle", null);
        }
        return "redirect:/admin/bottle/menu";
    }


    @GetMapping("/admin/add/user")
    public String addUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "admin/add-user";
    }

    @PostMapping("/admin/add/user")
    public String addUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (userRepository.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "This phone number is already in use.");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "admin/add-user";
        }
        Role roleOperator = roleRepository.findByRoleName(RoleName.ROLE_OPERATOR);
        user.setRoles(List.of(roleOperator));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/admin/delete/user/{id}")
    public String deleteUser(@PathVariable UUID id, Model model) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

            Set<Role> updatedRoles = user.getRoles().stream()
                    .filter(role -> !role.getRoleName().equals(RoleName.ROLE_OPERATOR))
                    .collect(Collectors.toSet());
            if (updatedRoles.isEmpty()) {
                userRepository.delete(user);
                return "redirect:/admin";
            }
            user.setRoles(List.copyOf(updatedRoles));
            userRepository.save(user);

            return "redirect:/admin";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Foydalanuvchini rollarini o'zgartirishda xatolik yuz berdi.");
            return "redirect:/admin";
        }
    }



    @GetMapping("/admin/edit/user/{id}")
    public String editUser(@PathVariable UUID id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/admin/edit/user/{id}")
    public String updateUser(@PathVariable UUID id, @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/edit-user";
        }
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setActive(user.getActive());
        existingUser.setPaid(user.getPaid());

        if (user.getPaid()) { // Agar foydalanuvchi to'lov qilgan bo'lsa
            existingUser.setPaidDate(LocalDate.now()); // To'lov sanasini yangilash
            existingUser.setNextMonthDate(LocalDate.now().plusMonths(1)); // Keyingi to'lov sanasi
        }

        userRepository.save(existingUser);
        return "redirect:/admin";
    }



}
