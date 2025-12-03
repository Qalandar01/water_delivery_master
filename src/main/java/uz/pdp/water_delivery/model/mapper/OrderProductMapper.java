package uz.pdp.water_delivery.model.mapper;

import uz.pdp.water_delivery.model.dto.OrderProductDto;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.records.OrderResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderProductMapper {

    public static List<OrderResponseDTO> mapToResponseDTO(List<OrderProduct> orderProducts) {

        if (orderProducts == null || orderProducts.isEmpty()) {
            return List.of();
        }

        // Group by order ID
        Map<Long, List<OrderProduct>> grouped =
                orderProducts.stream()
                        .collect(Collectors.groupingBy(op -> op.getOrder().getId()));

        // Convert each order group into one OrderResponseDTO
        return grouped.values().stream()
                .map(OrderProductMapper::mapSingleOrder)
                .toList();
    }

    private static OrderResponseDTO mapSingleOrder(List<OrderProduct> orderProducts) {
        Order order = orderProducts.get(0).getOrder();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderStatus(),
                order.getLocation(),
                LocalDate.now(),
                order.getCreatedAt(),
                order.getPhone(),
                mapProducts(orderProducts)
        );
    }

    private static List<OrderProductDto> mapProducts(List<OrderProduct> products) {
        return products.stream()
                .map(op -> new OrderProductDto(
                        op.getProduct().getType(),
                        op.getAmount()
                ))
                .toList();
    }

}
