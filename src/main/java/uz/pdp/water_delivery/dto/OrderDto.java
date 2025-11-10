package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.OrderProduct;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        OrderStatus status,
        Location location,
        LocalDate day,
        LocalDateTime date,
        String phone,
        List<OrderProductDto> products
) {


}
