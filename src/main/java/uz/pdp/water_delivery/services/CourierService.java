package uz.pdp.water_delivery.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.model.dto.response.CourierDTO;
import uz.pdp.water_delivery.model.dto.request.UserDTO;
import uz.pdp.water_delivery.model.entity.Role;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.model.enums.CourierStatus;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.enums.RoleName;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final TelegramBot telegramBot;
    private final DeleteMessageService deleteMessageService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<CourierDTO> getAllCouriersWithOrderStatus() {
        return courierRepository.findAll().stream()
                .map(courier -> new CourierDTO(
                        courier,
                        courier.getDistricts(),
                        orderRepository.existsByCourier(courier)
                ))
                .toList();
    }

    @Transactional
    public void saveCourier(UserDTO dto) {

        String phone = PhoneRepairUtil.repair(dto.getPhone());

        User user = userRepository.findByPhone(phone).orElse(null);

        // User exists → but already a courier
        if (user != null && courierRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Bunday telefon raqami bilan Courier mavjud.");
        }

        // Create or update user
        user = prepareUser(user, phone, dto);
        userRepository.save(user);

        // Assign role
        assignDeliveryRole(user);

        // Create courier
        Courier courier = buildCourier(dto, user);
        courierRepository.save(courier);

        // Notify
        notifyCourierAccepted(user);
    }

    private User prepareUser(User user, String phone, UserDTO dto) {
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setPassword(null); // no password for courier
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return user;
    }

    private void assignDeliveryRole(User user) {
        Role deliveryRole = roleRepository.findByRoleName(RoleName.ROLE_DELIVERY);
        if (deliveryRole == null) {
            throw new IllegalStateException("ROLE_DELIVERY roli topilmadi.");
        }

        if (!user.getRoles().contains(deliveryRole)) {
            user.getRoles().add(deliveryRole);
        }
    }

    private Courier buildCourier(UserDTO dto, User user) {
        return Courier.builder()
                .courierStatus(CourierStatus.WAITING)
                .carNumber(dto.getCarNumber())
                .carType(dto.getCarType())
                .districts(null)
                .isDeleted(false)
                .isActive(dto.isActive())
                .user(user)
                .build();
    }

    private void notifyCourierAccepted(User user) {
        telegramUserRepository.findByUser(user).ifPresent(telegramUser -> {

            String messageText = """
                    ✅ Siz kuryerlikka qabul qilindingiz.
                    Ishni boshlash uchun Menudan "Qayta boshlash 🔄" buyrug'ini yuboring.
                    """;

            SendMessage message = new SendMessage(telegramUser.getChatId(), messageText);
            SendResponse response = telegramBot.execute(message);

            deleteMessageService.archivedForDeletingMessages(
                    telegramUser,
                    response.message().messageId(),
                    messageText
            );
        });
    }

    public Courier findByIdOrThrow(Long id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid courier id: " + id));
    }

    @Transactional
    public void updateCourier(Long id, UserDTO updatedCourier) {
        Courier existingCourier = courierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Noto'g'ri kuryer ID: " + id));

        String repairedPhone = PhoneRepairUtil.repair(updatedCourier.getPhone());

        // Check if phone is already used by another courier
        boolean phoneExistsForAnother = courierRepository.existsByUserPhoneAndIdNot(repairedPhone, id);
        if (phoneExistsForAnother) {
            throw new IllegalStateException("Ushbu telefon raqami boshqa kuryer tomonidan foydalanilmoqda.");
        }

        // Check if courier has active assigned orders before deactivating
        boolean hasAssignedOrders = orderRepository.existsByCourierAndOrderStatus(existingCourier, OrderStatus.ASSIGNED);
        if (hasAssignedOrders) {
            throw new IllegalStateException(
                    "Kuryer faol buyurtmalarni boshqarayotganligi sababli faoliyat holatini o'zgartirib bo'lmaydi."
            );
        }

        // Update user info
        existingCourier.getUser().setFirstName(updatedCourier.getFirstName());
        existingCourier.getUser().setLastName(updatedCourier.getLastName());
        existingCourier.getUser().setPhone(repairedPhone);

        // Update courier info
        existingCourier.setCarType(updatedCourier.getCarType());
        existingCourier.setCarNumber(updatedCourier.getCarNumber());
        existingCourier.setIsActive(updatedCourier.isActive());
        existingCourier.setDistricts(null);

        courierRepository.save(existingCourier);
    }

    @Transactional
    public void deleteCourierById(Long id) {

        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid courier ID: " + id));

        // Check if courier has active assigned orders
        boolean hasAssignedOrders = orderRepository.existsByCourierAndOrderStatus(courier, OrderStatus.ASSIGNED);
        if (hasAssignedOrders) {
            throw new IllegalStateException(
                    "Kuryerni o'chirib bo'lmaydi, chunki u bilan bog'liq buyurtmalar mavjud."
            );
        }

        // Check if Telegram user has orders
        telegramUserRepository.findByUser(courier.getUser()).ifPresent(telegramUser -> {
            boolean hasTelegramOrders = orderRepository.existsByTelegramUser(telegramUser);
            if (hasTelegramOrders) {
                throw new IllegalStateException(
                        "Kuryerni o'chirib bo'lmaydi, chunki u bilan bog'liq buyurtmalar mavjud."
                );
            }
            // Delete Telegram user
            telegramUser.setIsDeleted(true);
            telegramUserRepository.save(telegramUser);
        });

        // Delete courier and associated user
        courier.setIsDeleted(true);
        courierRepository.save(courier);
        courier.getUser().setIsDeleted(true);
        userRepository.save(courier.getUser());
    }
}
