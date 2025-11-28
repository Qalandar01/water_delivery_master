package uz.pdp.water_delivery.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.dto.OrderSummaryDTO;
import uz.pdp.water_delivery.model.dto.UpdateOrderPageDTO;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.model.entity.CurrentOrders;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.exception.CourierNotFoundException;
import uz.pdp.water_delivery.exception.OrderNotFoundException;
import uz.pdp.water_delivery.model.repo.CourierRepository;
import uz.pdp.water_delivery.model.repo.CurrentOrdersRepository;
import uz.pdp.water_delivery.model.repo.OrderProductRepository;
import uz.pdp.water_delivery.model.repo.OrderRepository;

import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final CurrentOrdersRepository currentOrdersRepository;
    private final OrderProductRepository orderProductRepository;
    private final TelegramBot telegramBot;
    private final DeleteMessageService deleteMessageService;
    private final BotService botService;

    public OrderService(OrderRepository orderRepository, CourierRepository courierRepository, CurrentOrdersRepository currentOrdersRepository, OrderProductRepository orderProductRepository, TelegramBot telegramBot, DeleteMessageService deleteMessageService, BotService botService) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.currentOrdersRepository = currentOrdersRepository;
        this.orderProductRepository = orderProductRepository;
        this.telegramBot = telegramBot;
        this.deleteMessageService = deleteMessageService;
        this.botService = botService;
    }

    @Transactional
    public void assignOrdersToCourier(String ordersJson, Long courierId) throws Exception {
        Long[] orderIds = new ObjectMapper().readValue(ordersJson, Long[].class);
        List<Order> orders = orderRepository.findAllById(Arrays.asList(orderIds));

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found"));

        List<CurrentOrders> currentOrders = new ArrayList<>();

        // Get last order count for the courier
        int orderCountStart = currentOrdersRepository
                .getLastCount(courier.getId(), OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE)
                .orElse(0) + 1;

        for (Order order : orders) {
            CurrentOrders currentOrder = new CurrentOrders();
            currentOrder.setOrder(order);
            currentOrder.setOrderCount(orderCountStart);
            currentOrders.add(currentOrder);

            order.setCourier(courier);
            order.setOrderStatus(OrderStatus.ASSIGNED);

            orderCountStart++;
        }

        orderRepository.saveAll(orders);
        currentOrdersRepository.saveAll(currentOrders);
    }

    @Transactional
    public Long unassignOrder(Long orderId) {
        CurrentOrders currentOrder = currentOrdersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        Order order = currentOrder.getOrder();
        Long courierId = order.getCourier() != null ? order.getCourier().getId() : null;

        // Reset order
        order.setOrderStatus(OrderStatus.CREATED);
        order.setCourier(null);
        orderRepository.save(order);

        // Remove from current orders
        currentOrdersRepository.deleteById(currentOrder.getId());

        return courierId;
    }

    public UpdateOrderPageDTO getUpdateOrderPageData(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());


        return new UpdateOrderPageDTO(order, orderProducts);
    }

    @Transactional
    public void updateOrderDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

         order.setCourier(null); // reset courier assignment
        order.setOrderStatus(OrderStatus.CREATED);

        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        // Update order status and unassign courier
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setCourier(null);
        orderRepository.save(order);

        TelegramUser tgUser = order.getTelegramUser();

        // Notify user via Telegram
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                "❌ Buyurtmangiz bekor qilindi"
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();

        // Archive message for deletion
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, "Buyurtmangiz bekor qilindi");

        // Reset user state and update cabinet
        tgUser.setState(TelegramState.CABINET);
        botService.sendCabinet(tgUser);
    }

    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }


    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCourier(null); // unassign courier
        orderRepository.save(order);
    }

    public List<Order> getOrdersWithPhoneOff() {
        return orderRepository.findAllByOrderStatus(OrderStatus.PHONE_OFF);
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


    @Transactional
    public void assignCouriersToOrders() {
        List<Courier> couriers = courierRepository.findAll();
        List<Order> orders = orderRepository.findAllByOrderStatus(OrderStatus.CREATED);
        Map<String, Integer> courierIndexesByDistrict = new HashMap<>();

        List<Order> ordersToUpdate = new ArrayList<>();

        for (Order order : orders) {
            if (order.getCourier() != null) continue;

            String orderDistrict = order.getDistrict();

            List<Courier> matchingCouriers = couriers.stream()
                    .filter(c -> c.getDistricts().contains(orderDistrict))
                    .toList();

            if (matchingCouriers.isEmpty()) continue;

            Courier selectedCourier;

            if (matchingCouriers.size() == 1) {
                selectedCourier = matchingCouriers.get(0);
            } else {
                int currentIndex = courierIndexesByDistrict.getOrDefault(orderDistrict, 0);
                selectedCourier = matchingCouriers.get(currentIndex);
                courierIndexesByDistrict.put(orderDistrict, (currentIndex + 1) % matchingCouriers.size());
            }

            order.setCourier(selectedCourier);
            order.setOrderStatus(OrderStatus.ASSIGNED);
            ordersToUpdate.add(order);
        }

        if (!ordersToUpdate.isEmpty()) {
            orderRepository.saveAll(ordersToUpdate); // batch update
        }
    }


    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByOrderStatus(status);
    }
}
