package uz.pdp.water_delivery.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.model.dto.CompanyDTO;
import uz.pdp.water_delivery.model.dto.CurrentOrdersDTO;
import uz.pdp.water_delivery.model.dto.OrderProductDto;
import uz.pdp.water_delivery.model.dto.OrdersPageData;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.enums.OrderStatus;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.mapper.OrderProductMapper;
import uz.pdp.water_delivery.model.records.OrderResponseDTO;
import uz.pdp.water_delivery.model.repo.*;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorService {

    private final TelegramUserRepository telegramUserRepository;
    private final CurrentOrdersRepository currentOrdersRepository;
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    public List<SimpleWaitingUser> getWaitingUsers() {
        return telegramUserRepository
                .findAllByStateInOrderByCreatedAt(List.of(TelegramState.WAITING_OPERATOR));
    }

    public Integer countUsersWithChangedLocation() {

        return telegramUserRepository.countByChangeLocation(true);
    }

    @Transactional(readOnly = true)
    public OrdersPageData getOrdersPageData(Long courierId) {
        log.info("🔍 Loading orders page data for courier ID: {}", courierId);

        // 1. Get all active couriers
        List<Courier> couriers = courierRepository.findAllByIsActive(true);
        log.info("✅ Found {} active couriers", couriers.size());

        if (couriers.isEmpty()) {
            throw new IllegalStateException("No active couriers found");
        }

        // 2. Find selected courier
        Courier currentCourier = findSelectedCourier(couriers, courierId);
        log.info("✅ Current courier ID: {}", currentCourier.getId());

        // 3. Get currently assigned orders (for drawing green lines on map)
        List<CurrentOrdersDTO> currentOrders = currentOrdersRepository
                .findSortedOrders(OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE)
                .stream()
                .map(o -> new CurrentOrdersDTO(
                        o.getOrder().getId(),
                        o.getOrder().getLocation(),
                        o.getOrder().getCourier().getId()
                ))
                .toList();

        log.info("✅ Found {} currently assigned orders", currentOrders.size());

        // 4. Company info
        CompanyDTO company = new CompanyDTO(
                "Shift Academy",
                69.280697,
                41.327692
        );

        // 5. ⚠️ CRITICAL FIX: Get ALL orders (not OrderProducts!)
        // Query Order entities directly, not OrderProduct
        List<Order> allOrders = orderRepository.findAllByOrderStatusIn(
                List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED, OrderStatus.WAITING_PHONE)
        );

        log.info("📦 Total orders from database: {}", allOrders.size());

        // Log status breakdown
        long pendingCount = allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.CREATED).count();
        long assignedCount = allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.ASSIGNED).count();
        long waitingCount = allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.WAITING_PHONE).count();

        log.info("📊 Status breakdown - PENDING: {}, ASSIGNED: {}, WAITING_PHONE: {}",
                pendingCount, assignedCount, waitingCount);

        // 6. Convert Orders to OrderResponseDTO
        List<OrderResponseDTO> orderResponseDTOS = allOrders.stream()
                .map(this::mapOrderToResponseDTO)
                .filter(dto -> dto != null && dto.location() != null)
                .collect(Collectors.toList());

        log.info("✅ Mapped {} orders to DTOs", orderResponseDTOS.size());

        // Log orders without location
        long noLocation = allOrders.stream()
                .filter(o -> o.getLocation() == null ||
                        o.getLocation().getLatitude() == null ||
                        o.getLocation().getLongitude() == null)
                .count();

        if (noLocation > 0) {
            log.warn("⚠️ {} orders missing location data - they won't appear on map!", noLocation);
            allOrders.stream()
                    .filter(o -> o.getLocation() == null ||
                            o.getLocation().getLatitude() == null ||
                            o.getLocation().getLongitude() == null)
                    .forEach(o -> log.warn("   Order #{} has no location", o.getId()));
        }

        return new OrdersPageData(
                couriers,
                currentCourier,
                currentOrders,
                company,
                orderResponseDTOS
        );
    }


    private Courier findSelectedCourier(List<Courier> couriers, Long id) {
        if (id == null) return couriers.get(0);
        return couriers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(couriers.get(0));
    }

    private OrderResponseDTO mapOrderToResponseDTO(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderProduct> all = orderProductRepository.findAllByOrderId((order.getId()));

        List<OrderProductDto> list = all.stream()
                .filter(op -> op.getProduct() != null)
                .map(op -> new OrderProductDto(
                        op.getProduct().getType(),
                        op.getAmount()
                ))
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderStatus(),
                order.getLocation(), // Already embedded Location object
                order.getCreatedAt() != null ? order.getCreatedAt().toLocalDate() : null,
                order.getCreatedAt(),
                order.getPhone(),
                list
        );
    }
}
