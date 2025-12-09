package uz.pdp.water_delivery.controller.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.UserBotService;
import uz.pdp.water_delivery.exception.CourierNotFoundException;
import uz.pdp.water_delivery.exception.OrderNotFoundException;
import uz.pdp.water_delivery.exception.TelegramUserNotFoundException;
import uz.pdp.water_delivery.exception.UserDeletionException;
import uz.pdp.water_delivery.model.dto.OrdersPageData;
import uz.pdp.water_delivery.model.dto.UpdateOrderPageDTO;
import uz.pdp.water_delivery.model.dto.VerifyUserDTO;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.repo.CourierRepository;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;
import uz.pdp.water_delivery.services.OperatorService;
import uz.pdp.water_delivery.services.OrderService;
import uz.pdp.water_delivery.services.TelegramUserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OperatorController {

    private final TelegramUserRepository telegramUserRepository;
    private final CourierRepository courierRepository;
    private final LogErrorFile logErrorFile;
    private final OperatorService operatorService;
    private final TelegramUserService telegramUserService;
    private final OrderService orderService;
    private final UserBotService userBotService;

    public String ApiKey = "23c60e9b-0d03-4854-b8cc-b1ef6ae33d78";

    @GetMapping("/operator")
    public String operatorMenu(Model model) {
        model.addAttribute("users", operatorService.getWaitingUsers());
        model.addAttribute("countTelegramUser", operatorService.countUsersWithChangedLocation());

        return "operator/menu";
    }

    @GetMapping("/operator/orders")
    public String orders(
            @RequestParam(required = false) Long courierId,
            Model model
    ) throws JsonProcessingException {
        try {
            OrdersPageData data = operatorService.getOrdersPageData(courierId);

            if (data == null) {
                throw new IllegalStateException("Failed to retrieve orders page data");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String ordersJson = objectMapper.writeValueAsString(data.getOrders());
            String currentOrdersJson = objectMapper.writeValueAsString(data.getCurrentOrders());
            String couriersJson = objectMapper.writeValueAsString(data.getCouriers());
            String companyJson = objectMapper.writeValueAsString(data.getCompany());
            String currentCourierJson = objectMapper.writeValueAsString(data.getCurrentCourier());

            model.addAttribute("ordersJson", ordersJson);
            model.addAttribute("currentOrdersJson", currentOrdersJson);
            model.addAttribute("couriersJson", couriersJson);
            model.addAttribute("companyJson", companyJson);
            model.addAttribute("currentCourierJson", currentCourierJson);
            model.addAttribute("yandexMapsApiKey", ApiKey);

            model.addAttribute("couriers", data.getCouriers());
            model.addAttribute("currentCourier", data.getCurrentCourier());
            model.addAttribute("currentOrders", data.getCurrentOrders());
            model.addAttribute("company", data.getCompany());
            model.addAttribute("orders", data.getOrders());

            System.out.println("Orders loaded: " + data.getOrders().size());
            System.out.println("Current courier: " + data.getCurrentCourier().getFullName());

            return "operator/orderMenu";
        } catch (JsonProcessingException e) {
            System.err.println("JSON serialization error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load orders data");
            return "error/500";
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An unexpected error occurred");
            return "error/500";
        }
    }

    @GetMapping("/operator/currentUser/{userId}")
    public String userVerify(@PathVariable Long userId, Model model) {
        try {
            TelegramUser user = telegramUserService.getUserById(userId);
            model.addAttribute("userInfo", user);
            return "operator/currentUser";
        } catch (TelegramUserNotFoundException e) {
            return "redirect:/operator?error=userNotFound";
        }
    }

    @PostMapping("/operator/wrong/location")
    public String wrongLocation(@RequestParam("userId") Long tgUserId) {
        Optional<TelegramUser> tgUserOpt = telegramUserRepository.findById(tgUserId);

        if (tgUserOpt.isEmpty()) {
            logErrorFile.logError(new RuntimeException("tg user not found"), "wrongLocation", tgUserId);
            return "redirect:/operator?error=userNotFound";
        }

        try {
            userBotService.sendLocationButton(tgUserOpt.get());
            return "redirect:/operator";
        } catch (Exception e) {
            logErrorFile.logError(e, "wrongLocation", tgUserId);
            return "redirect:/operator?error=locationIssue";
        }
    }


    @PostMapping("/operator/verify")
    public String verify(@ModelAttribute VerifyUserDTO verifyUserDTO) {
        try {
            telegramUserService.verifyUser(verifyUserDTO);
            return "redirect:/operator";
        } catch (TelegramUserNotFoundException e) {
            return "redirect:/operator?error=userNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "verify", verifyUserDTO.getTgUserId());
            return "redirect:/operator?error=verificationFailed";
        }
    }

    @PostMapping("/operator/noPhone")
    public String noPhone(@ModelAttribute VerifyUserDTO verifyUserDTO) {
        try {
            telegramUserService.handleNoPhone(verifyUserDTO);
            return "redirect:/operator";
        } catch (TelegramUserNotFoundException e) {
            return "redirect:/operator?error=userNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "noPhone", verifyUserDTO.getTgUserId());
            return "redirect:/operator?error=noPhoneError";
        }
    }


    @PostMapping("/operator/orders/assign")
    public String assignOrderToCourier(@RequestParam String orders,
                                       @RequestParam Long courierId) {
        try {
            orderService.assignOrdersToCourier(orders, courierId);
        } catch (CourierNotFoundException e) {
            return "redirect:/operator/orders?error=courierNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "assignOrderToCourier", courierId.hashCode(), null);
            return "redirect:/operator/orders?error=assignmentFailed";
        }
        return "redirect:/operator/orders?courier=" + courierId;
    }


    @PostMapping("/operator/orders/unassign")
    public String unAssignOrderFromCourier(@RequestParam Long orderId) {
        Long courierId = null;
        try {
            courierId = orderService.unassignOrder(orderId);
        } catch (OrderNotFoundException e) {
            return "redirect:/operator/orders?error=orderNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "unAssignOrderFromCourier", orderId.hashCode(), null);
            return "redirect:/operator/orders?error=unassignFailed";
        }
        return "redirect:/operator/orders?courier=" + courierId;
    }


    @GetMapping("/operator/update/order/{orderId}")
    public String updateOrderPage(@PathVariable Long orderId, Model model) {
        try {
            UpdateOrderPageDTO dto = orderService.getUpdateOrderPageData(orderId);
            model.addAttribute("orderProducts", dto.getOrderProducts());
            model.addAttribute("availableTimes", null);
            model.addAttribute("order", dto.getOrder());
            model.addAttribute("selectedTimeId", null);

            return "operator/updateOrder";
        } catch (OrderNotFoundException e) {
            return "redirect:/operator/orders?error=orderNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateOrderPage", orderId.hashCode(), null);
            return "redirect:/operator/orders?error=internalError";
        }
    }


    @PutMapping("/operator/update/order")
    public String updateOrder(@RequestParam Long orderId,
                              @RequestParam Long deliveryTimeId) {
        try {
            orderService.updateOrderDelivery(orderId);
            return "redirect:/operator/orders?success=orderUpdated";
        } catch (OrderNotFoundException e) {
            return "redirect:/operator/update/order/" + orderId + "?error=orderNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders?error=updateFailed";
        }
    }


    @PostMapping("/operator/cancel/order")
    public String cancelOrder(@RequestParam Long orderId, Model model) {
        try {
            orderService.cancelOrder(orderId);
            return "redirect:/operator/orders?success=orderCanceled";
        } catch (OrderNotFoundException e) {
            return "redirect:/operator/update/order/" + orderId + "?error=orderNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "cancelOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders?error=cancellationFailed";
        }
    }

    @GetMapping("/operator/all/orders")
    public String allOrders(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<Order> ordersPage = orderService.getAllOrders(page, size);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "operator/all-orders";
    }


    @PostMapping("/operator/complete/order")
    public String completeOrder(@RequestParam Long orderId, Model model) {
        try {
            orderService.completeOrder(orderId);
            return "redirect:/operator/orders?success=orderCompleted";
        } catch (OrderNotFoundException e) {
            return "redirect:/operator/update/order/" + orderId + "?error=orderNotFound";
        } catch (Exception e) {
            logErrorFile.logError(e, "completeOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders?error=completeFailed";
        }
    }


    @GetMapping("/operator/users")
    public String usersWithRoleUser(Model model) {
        List<TelegramUser> telegramUsers = telegramUserService.getAllUsersWithRoleUser();
        model.addAttribute("users", telegramUsers);
        return "operator/all-users";
    }


    @GetMapping("/operator/users/search")
    @ResponseBody
    public List<TelegramUser> searchUsers(@RequestParam("keyword") String keyword) {
        return telegramUserService.searchUsersByKeyword(keyword);
    }


    @GetMapping("/operator/userdeleted/{userId}")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            telegramUserService.softDeleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (TelegramUserNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        } catch (UserDeletionException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete user because related data exists.");
        } catch (Exception e) {
            logErrorFile.logError(e, "deleteUser", userId.hashCode(), null);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred while deleting user.");
        }
        return "redirect:/operator/users";
    }


    @GetMapping("/operator/users/notActivePhones")
    public String listUsersWithInactivePhones(Model model) {
        List<TelegramUser> telegramUsers = telegramUserService.getUsersWithPhoneOff();
        model.addAttribute("users", telegramUsers);
        return "operator/all-users-phone-off";
    }

//    @GetMapping("/operator/orders/payment")
//    public String showPaymentOrders(Model model) {
//        LocalDate today = LocalDate.now();
//        model.addAttribute("todayDate", today);
//
//        PaymentOrdersDTO paymentOrdersDTO = orderService.getPaymentOrdersForToday();
//        model.addAttribute("deliveryTimes", null);
//        model.addAttribute("orderSummary", paymentOrdersDTO.orderSummary());
//
//        return "operator/orders-payment";
//    }


    @GetMapping("/operator/update/courier/{courierId}")
    public String checkCourier(@PathVariable Long courierId, Model model) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(() -> new IllegalArgumentException("Invalid courier Id:" + courierId));
        model.addAttribute("courier", courier);
        return "operator/orders-payment";
    }

    @GetMapping("/operator/orders/upload/couriers")
    public String assignCouriersToOrders() {
        try {
            orderService.assignCouriersToOrders();
        } catch (Exception e) {
            logErrorFile.logError(e, "assignCouriersToOrders", null);
        }
        return "redirect:/operator/orders";
    }


    @GetMapping("/operator/orders/time-out")
    public String listTimeOutOrders(Model model) {
        List<Order> orders = orderService.getOrdersByStatus(OrderStatus.END_TIME);
        model.addAttribute("orders", orders);
        return "operator/time-over-orders";
    }


    @GetMapping("/operator/users/change-location-users")
    public String listUsersRequestingLocationChange(Model model) {
        List<SimpleWaitingUser> users = telegramUserService.getUsersRequestingLocationChange();
        model.addAttribute("users", users);
        return "operator/change-location-users";
    }


}
