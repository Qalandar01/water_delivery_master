package uz.pdp.water_delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.dto.*;
import uz.pdp.water_delivery.entity.*;
import uz.pdp.water_delivery.entity.enums.OrderStatus;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.services.service.DeleteMessageService;
import uz.pdp.water_delivery.utils.Base64Utils;
import uz.pdp.water_delivery.utils.DistrictUtil;
import uz.pdp.water_delivery.utils.LogErrorFile;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OperatorController {

    private final TelegramUserRepository telegramUserRepository;
    private final DistrictRepository districtRepository;
    private final DistrictUtil districtUtil;
    private final BotServiceIn botServiceIn;
    private final OrderRepository orderRepository;
    private final RegionRepository regionRepository;
    private final CourierRepository courierRepository;
    private final DeliveryTimeRepository deliveryTimeRepository;
    private final HttpSession httpSession;
    private final UserRepository userRepository;
    private final BottleTypesRepository bottleTypesRepository;
    private final LogErrorFile logErrorFile;
    private final Base64Utils base64Utils;
    private final TelegramBot telegramBot;
    private final DeleteMessageService deleteMessageService;
    private final OrderProductRepository orderProductRepository;
    private final ObjectMapper objectMapper;
    private final CurrentOrdersRepository currentOrdersRepository;

    public String ApiKey = "23c60e9b-0d03-4854-b8cc-b1ef6ae33d78";

    @GetMapping("/operator")
    public String registration(Model model) {
        List<TelegramState> states = List.of(TelegramState.WAITING_OPERATOR);
        List<SimpleWaitingUser> users = telegramUserRepository.findAllByStateInOrderByCreatedAt(states);
        Integer count = telegramUserRepository.countByChangeLocation(true);
        model.addAttribute("users", users);
        model.addAttribute("countTelegramUser", count);
        return "operator/menu";
    }

    @GetMapping("/operator/orders")
    public String orders(
            Model model,
            @RequestParam(defaultValue = "") UUID courier
    ) {
        List<Courier> couriers = courierRepository.findAllByIsActive(true);
        List<Order> orders = orderRepository.findAllByOrderStatusIn(List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE));
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderIn(orders);
//        List<OrderDto> list = orders.stream().map(item -> new OrderDto(
//                item.getId(),
//                item.getOrderStatus(),
//                item.getLocation(),
//                item.getDay(),
//                item.getCreatedAt(),
//                item.getPhone(),
//                OrderProductDto.makeListFromEntity(orderProducts
//                        .stream()
//                        .filter(orderProduct -> orderProduct.getOrder().getId().equals(item.getId()))
//                        .toList())
//        )).toList();

//        List<OrderDto> orders = List.of(
//                new OrderDto(100L, OrderStatus.CREATED, new Location(41.310, 69.290), LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(1), "998901111111", List.of(new OrderProductDto("20l", 5), new OrderProductDto("10l", 5))),
//                new OrderDto(200L, OrderStatus.CREATED, new Location(41.312, 69.295), LocalDate.now(), LocalDateTime.now(), "998902222222", List.of(new OrderProductDto("20l", 10))),
//                new OrderDto(300L, OrderStatus.CREATED, new Location(41.315, 69.300), LocalDate.now(), LocalDateTime.now(), "998903333333", List.of(new OrderProductDto("20l", 4))),
//                new OrderDto(400L, OrderStatus.ASSIGNED, new Location(41.318, 69.305), LocalDate.now(), LocalDateTime.now(), "998904444444", List.of(new OrderProductDto("20l", 3))),
//                new OrderDto(500L, OrderStatus.ASSIGNED, new Location(41.320, 69.310), LocalDate.now(), LocalDateTime.now(), "998905555555", List.of(new OrderProductDto("20l", 7))),
//                new OrderDto(600L, OrderStatus.ASSIGNED, new Location(41.322, 69.315), LocalDate.now(), LocalDateTime.now(), "998906666666", List.of(new OrderProductDto("20l", 8))),
//                new OrderDto(700L, OrderStatus.CREATED, new Location(41.325, 69.320), LocalDate.now(), LocalDateTime.now(), "998907777777", List.of(new OrderProductDto("20l", 2))),
//                new OrderDto(800L, OrderStatus.CREATED, new Location(41.328, 69.325), LocalDate.now(), LocalDateTime.now(), "998908888888", List.of(new OrderProductDto("20l", 10))),
//                new OrderDto(900L, OrderStatus.CREATED, new Location(41.330, 69.330), LocalDate.now().minusDays(2), LocalDateTime.now().minusDays(2), "998909999999", List.of(new OrderProductDto("20l", 15))),
//                new OrderDto(1000L, OrderStatus.CREATED, new Location(41.332, 69.335), LocalDate.now(), LocalDateTime.now(), "998901010101", List.of(new OrderProductDto("20l", 20)))
//        );
        Optional<Courier> currentCourierOpt = couriers.stream().filter(c -> c.getId().equals(courier)).findFirst();
        Courier currentCourier = currentCourierOpt.orElseGet(() -> couriers.get(0));

        List<CurrentOrders> currentOrders = currentOrdersRepository.findSortedOrders(OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE);
        List<CurrentOrdersDTO> currentOrdersDTOS = currentOrders.stream().map(item -> new CurrentOrdersDTO(item.getOrder().getId(), item.getOrder().getLocation(), item.getOrder().getCourier().getId())).toList();
//        List<CurrentOrdersDTO> currentOrders = new ArrayList<>();
//        currentOrders.add(new CurrentOrdersDTO(400L, new Location(41.318, 69.305), couriers.get(0).getId()));
//        currentOrders.add(new CurrentOrdersDTO(500L, new Location(41.320, 69.310), couriers.get(0).getId()));
//        currentOrders.add(new CurrentOrdersDTO(600L, new Location(41.322, 69.315), couriers.get(0).getId()));
        model.addAttribute("currentOrders", currentOrdersDTOS);

        model.addAttribute("yandexMapsApiKey", ApiKey);
        model.addAttribute("orders", new ArrayList<>());
        model.addAttribute("couriers", couriers);
        model.addAttribute("currentCourier", currentCourier);
        model.addAttribute("company", new CompanyDTO(
                "Shift Academy",
                69.280697,
                41.327692
        ));
        return "operator/orderMenu";
    }

    @Transactional
    @GetMapping("/operator/currentUser/{userId}")
    public String userVerify(@PathVariable("userId") Long tgUserId, Model model) {
        try {
            TelegramUser telegramUser = telegramUserRepository.findById(tgUserId)
                    .orElseThrow(() -> new RuntimeException("tg user not found"));
            model.addAttribute("userInfo", telegramUser);
            return "operator/currentUser";
        } catch (Exception e) {
            logErrorFile.logError(e, "userVerify", null);
            return "redirect:/operator?error=userNotFound";
        }
    }

    @PostMapping("/operator/wronglocation")
    public String wrongLocation(@RequestParam(name = "userId") Long tgUserId) {
        try {
            TelegramUser tgUser = telegramUserRepository.findById(tgUserId)
                    .orElseThrow(() -> new RuntimeException("tg user not found"));
            botServiceIn.sendLocationButton(tgUser);
            return "redirect:/operator";
        } catch (Exception e) {
            logErrorFile.logError(e, "wrongLocation", 0L);
            return "redirect:/operator?error=locationIssue";
        }
    }

    @Transactional
    @PostMapping("/operator/verify")
    public String verify(@ModelAttribute VerifyUserDTO verifyUserDTO) {
        try {
            TelegramUser tgUser = telegramUserRepository.findById(verifyUserDTO.getTgUserId())
                    .orElseThrow(() -> new RuntimeException("tg user not found"));
            tgUser.setLocation(new Location(verifyUserDTO.getLatitude(), verifyUserDTO.getLongitude()));
            tgUser.setAddressLine(verifyUserDTO.getAddressLine());
            if (!verifyUserDTO.isHome()) {
                tgUser.setHome(verifyUserDTO.isHome());
                tgUser.setXonadon(verifyUserDTO.getXonadon());
                tgUser.setPodyez(verifyUserDTO.getPodyez());
                tgUser.setQavat(verifyUserDTO.getQavat());
                tgUser.setKvRaqami(verifyUserDTO.getKvRaqami());
            } else {
                tgUser.setHome(verifyUserDTO.isHome());
            }
            tgUser.setVerified(true);
            tgUser.setPhoneOff(false);
            tgUser.setState(TelegramState.CABINET);
            tgUser.setChangeLocation(false);
            tgUser.getUser().setDoublePhone(verifyUserDTO.getPhone() != null ? verifyUserDTO.getPhone() : tgUser.getUser().getPhone());
            telegramUserRepository.save(tgUser);
            botServiceIn.sendCabinet(tgUser);
            return "redirect:/operator";
        } catch (Exception e) {
            logErrorFile.logError(e, "verify", 0L);
            return "redirect:/operator?error=verificationFailed";
        }
    }


    @Transactional
    @PostMapping("/operator/noPhone")
    public String noPhone(@ModelAttribute VerifyUserDTO verifyUserDTO) {
        try {
            TelegramUser tgUser = telegramUserRepository.findById(verifyUserDTO.getTgUserId())
                    .orElseThrow(() -> new RuntimeException("tg user not found"));

            if (verifyUserDTO.getLongitude() != null || verifyUserDTO.getLatitude() != null ||
                    verifyUserDTO.getAddressLine() != null || verifyUserDTO.getDistrictId() != null) {
                tgUser.setLocation(new Location(verifyUserDTO.getLatitude(), verifyUserDTO.getLongitude()));
                tgUser.setAddressLine(verifyUserDTO.getAddressLine());

                District district = districtRepository.findById(verifyUserDTO.getDistrictId())
                        .orElseThrow(() -> new RuntimeException("district not found"));
                tgUser.setDistrict(district);
            }

            tgUser.setPhoneOff(true);
            tgUser.setState(TelegramState.NO_PHONE);
            telegramUserRepository.save(tgUser);
            botServiceIn.sendUserDidNotAnswerPhone(tgUser);

            return "redirect:/operator";
        } catch (Exception e) {
            logErrorFile.logError(e, "noPhone", 0L);
            return "redirect:/operator?error=noPhoneError";
        }
    }

    @Transactional
    @PostMapping("/operator/orders/assign")
    public String assignOrderToCourier(
            @RequestParam(name = "orders") String ordersIdsJson,
            @RequestParam Long courierId) {
        try {
            Long[] orderIds = objectMapper.readValue(ordersIdsJson, Long[].class);
            List<Order> orders = orderRepository.findAllById(Arrays.asList(orderIds));
            Optional<Courier> courierOptional = courierRepository.findById(courierId);
            List<CurrentOrders> currentOrders = new ArrayList<>();
            if (courierOptional.isPresent()) {
                Courier courier = courierOptional.get();
                Optional<Integer> lastCountOpt = currentOrdersRepository.getLastCount(courier.getId(), OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE);
                int i = lastCountOpt.orElseGet(() -> 0) + 1;
                for (Order order : orders) {
                    CurrentOrders currentOrder = new CurrentOrders();
                    currentOrder.setOrder(order);
                    currentOrder.setOrderCount(i);
                    currentOrders.add(currentOrder);
                    order.setCourier(courier);
                    order.setOrderStatus(OrderStatus.ASSIGNED);
                    i++;
                }
                orderRepository.saveAll(orders);
                currentOrdersRepository.saveAll(currentOrders);
            } else {
                logErrorFile.logError(new Exception("Courier not found"), "assignOrderToCourier", courierId.hashCode(), null);
            }
        } catch (Exception e) {
            logErrorFile.logError(e, "assignOrderToCourier", null);
        }
        return "redirect:/operator/orders?courier=" + courierId;
    }

    @Transactional
    @PostMapping("/operator/orders/unassign")
    public String unAssignOrderFromCourier(
            @RequestParam Long orderId) {
        Optional<CurrentOrders> currentOrdersOptional = currentOrdersRepository.findByOrderId(orderId);
        Long currentCourierId = null;
        if (currentOrdersOptional.isPresent()) {
            var item = currentOrdersOptional.get();
            Order order = item.getOrder();
            currentCourierId = order.getCourier().getId();
            order.setOrderStatus(OrderStatus.CREATED);
            order.setCourier(null);
            orderRepository.save(order);
            currentOrdersRepository.deleteById(item.getId());
        }
        return "redirect:/operator/orders?courier=" + currentCourierId;
    }


    @GetMapping("/operator/update/order/{orderId}")
    public String updateOrderPage(@PathVariable("orderId") Long orderId, Model model) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Buyurtma topilmadi"));

            List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());
            model.addAttribute("orderProducts", orderProducts);

            List<DeliveryTime> allTimes = deliveryTimeRepository.findAll();
            DeliveryTime selectedTime = order.getDeliveryTime();

            LocalDateTime now = LocalDateTime.now();
            LocalTime currentTime = now.toLocalTime().plusMinutes(15);

            List<DeliveryTime> availableTimes = allTimes.stream()
                    .filter(time -> {
                        if ("Ertaga".equals(time.getDay())) {
                            return true;
                        }
                        return "Bugun".equals(time.getDay()) && time.getStartTime().isAfter(currentTime);
                    }).collect(Collectors.toList());

            model.addAttribute("availableTimes", availableTimes);
            model.addAttribute("order", order);
            model.addAttribute("selectedTimeId", selectedTime.getId());

            return "operator/updateOrder";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateOrderPage", orderId.hashCode(), null);
            return "redirect:/operator/orders";
        }
    }


    @Transactional
    @PostMapping("/operator/update/order")
    public String updateOrder(@RequestParam("orderId") Long orderId,
                              @RequestParam("deliveryTime") Long deliveryTimeId, Model model) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                DeliveryTime newDeliveryTime = deliveryTimeRepository.findById(deliveryTimeId)
                        .orElseThrow(() -> new RuntimeException("Delivery time not found"));
                if (newDeliveryTime.getDay().equals("Bugun")) {
                    order.setDay(LocalDate.now());
                } else {
                    order.setDay(LocalDate.now().plusDays(1));
                }
                order.setDeliveryTime(newDeliveryTime);
                order.setCourier(null);
                order.setOrderStatus(OrderStatus.CREATED);
                orderRepository.save(order);
                model.addAttribute("successMessage", "Buyurtma muvaffaqiyatli yangilandi");
            } else {
                model.addAttribute("errorMessage", "Buyurtma topilmadi");
                return "redirect:/operator/update/order/{orderId}" + orderId;
            }
            return "redirect:/operator/orders";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders";
        }
    }

    @Transactional
    @PostMapping("/operator/cancel/order")
    public String cancelOrder(@RequestParam("orderId") Long orderId, Model model) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                order.setOrderStatus(OrderStatus.CANCELED);
                order.setCourier(null);
                SendMessage sendMessage = new SendMessage(
                        order.getTelegramUser().getChatId(),
                        "❌ Buyurtmangiz bekor qilindi"
                );
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Integer messageId = sendResponse.message().messageId();
                deleteMessageService.archivedForDeletingMessages(order.getTelegramUser(), messageId, "Buyurtmangiz bekor qilindi");
                order.getTelegramUser().setState(TelegramState.CABINET);
                orderRepository.save(order);
                botServiceIn.sendCabinet(order.getTelegramUser());
                model.addAttribute("successMessage", "Buyurtma muvaffaqiyatli bekor qilindi");
            } else {
                model.addAttribute("errorMessage", "Buyurtma topilmadi");
                return "redirect:/operator/update/order/" + orderId;
            }
            return "redirect:/operator/orders";
        } catch (Exception e) {
            logErrorFile.logError(e, "cancelOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders";
        }
    }

    @GetMapping("/operator/all/orders")
    public String allOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "operator/all-orders";
    }


    @Transactional
    @PostMapping("/operator/complete/order")
    public String completeOrder(@RequestParam("orderId") Long orderId, Model model) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setCourier(null);
                orderRepository.save(order);
                model.addAttribute("successMessage", "Buyurtma muvaffaqiyatli tugatildi");
            } else {
                model.addAttribute("errorMessage", "Buyurtma topilmadi");
                return "redirect:/operator/update/order/{orderId}" + orderId;
            }
            return "redirect:/operator/orders";
        } catch (Exception e) {
            logErrorFile.logError(e, "completeOrder", orderId.hashCode(), null);
            return "redirect:/operator/orders";
        }
    }

    @GetMapping("/operator/users")
    public String usersWithRoleUser(Model model) {
        List<TelegramUser> telegramUsers = telegramUserRepository.findAllByRoleNameUsers();
        model.addAttribute("users", telegramUsers);
        return "operator/all-users";
    }


    @GetMapping("/operator/users/search")
    @ResponseBody
    public List<TelegramUser> searchUsers(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return telegramUserRepository.findAll();
        }
        return telegramUserRepository.searchByUserPhone(keyword);
    }


    @GetMapping("/operator/userdeleted/{userId}")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            if (telegramUserRepository.existsById(userId)) {
                telegramUserRepository.deleteById(userId);
                redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu mijozda ma'lumotlar mavjud! shuning uchun o'chirib bo'lmaydi!");
            logErrorFile.logError(e, "deleteUser", userId.hashCode(), null);
        }
        return "redirect:/operator/users";
    }


    @GetMapping("/operator/users/notActivePhones")
    public String notActiveUsersAll(Model model) {
        List<TelegramUser> telegramUsers = telegramUserRepository.findAllByPhoneOff();
        model.addAttribute("users", telegramUsers);
        return "operator/all-users-phone-off";
    }

    @GetMapping("/operator/orders/phone-off")
    public String notActiveOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderStatus(OrderStatus.PHONE_OFF);
        model.addAttribute("orders", orders);
        return "operator/orders-phoneOff";

    }

    @GetMapping("/operator/orders/payment")
    public String payment(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("todayDate", today);

        String bugun = "Bugun";
        List<DeliveryTime> deliveryTimes = deliveryTimeRepository.findAllByDayOrderByIdAsc(bugun);
        model.addAttribute("deliveryTimes", deliveryTimes);

        List<OrderSummaryDTO> orderSummary = getOrderSummary();
        model.addAttribute("orderSummary", orderSummary);

        return "operator/orders-payment";
    }

    public List<OrderSummaryDTO> getOrderSummary() {
        List<Object[]> results = orderRepository.countOrdersByCourierAndOrderStatus();
        List<OrderSummaryDTO> orderSummaryDTOs = new ArrayList<>();

        for (Object[] result : results) {
            String fullName = (String) result[0];
            String carType = (String) result[1];
            Long courierId = (Long) result[2];
            Integer deliveryTimeId = result[3] != null ? ((Number) result[3]).intValue() : null;
            Long orderSize = result[4] != null ? ((Number) result[4]).longValue() : 0L;
            Long completedOrders = result[5] != null ? ((Number) result[5]).longValue() : 0L;
            Long notAnswered = result[6] != null ? ((Number) result[6]).longValue() : 0L;
            Long notInTime = result[7] != null ? ((Number) result[7]).longValue() : 0L;
            Long bottlesCount = result[8] != null ? ((Number) result[8]).longValue() : 0L;

            OrderSummaryDTO dto = new OrderSummaryDTO(
                    fullName, carType, courierId, deliveryTimeId, orderSize,
                    completedOrders, notAnswered, notInTime, bottlesCount);
            orderSummaryDTOs.add(dto);
        }
        return orderSummaryDTOs;
    }


    @GetMapping("/operator/update/courier/{courierId}")
    public String checkCourier(@PathVariable Long courierId, Model model) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(() -> new IllegalArgumentException("Invalid courier Id:" + courierId));
        model.addAttribute("courier", courier);
        return "operator/orders-payment";
    }

    @GetMapping("/operator/orders/upload/couriers")
    public String uploadCourier() {
        try {
            List<Courier> couriers = courierRepository.findAll();
            List<Order> orders = orderRepository.findAllByOrderStatus(OrderStatus.CREATED);
            Map<Long, Integer> courierIndexesByDistrict = new HashMap<>();

            for (Order order : orders) {
                if (order.getCourier() == null) {
                    District orderDistrict = order.getDistrict();
                    List<Courier> matchingCouriers = couriers.stream()
                            .filter(courier -> courier.getDistricts().stream()
                                    .anyMatch(district -> district.getId().equals(orderDistrict.getId())))
                            .toList();

                    if (!matchingCouriers.isEmpty()) {
                        Courier selectedCourier;

                        if (matchingCouriers.size() == 1) {
                            selectedCourier = matchingCouriers.get(0);
                        } else {
                            int currentIndex = courierIndexesByDistrict.getOrDefault(orderDistrict.getId(), 0);
                            selectedCourier = matchingCouriers.get(currentIndex);

                            courierIndexesByDistrict.put(orderDistrict.getId(), (currentIndex + 1) % matchingCouriers.size());
                        }

                        order.setOrderStatus(OrderStatus.ASSIGNED);
                        order.setCourier(selectedCourier);
                        orderRepository.save(order);
                    }
                }
            }
            return "redirect:/operator/orders";
        } catch (Exception e) {
            logErrorFile.logError(e, "uploadCourier", null);
        }
        return "redirect:/operator/orders";
    }


    @GetMapping("/operator/orders/time-out")
    public String ordersTimeOut(Model model) {
        List<Order> orders = orderRepository.findAllByOrderStatus(OrderStatus.END_TIME);
        model.addAttribute("orders", orders);
        return "operator/time-over-orders";
    }

    @GetMapping("/operator/users/change-location-users")
    public String changeLocationUsers(Model model) {
        List<SimpleWaitingUser> telegramUsers = telegramUserRepository.findAllByChangeLocation(true);
        model.addAttribute("users", telegramUsers);
        return "operator/change-location-users";
    }


}
