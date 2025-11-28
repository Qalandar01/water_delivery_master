package uz.pdp.water_delivery.model.dto;

import uz.pdp.water_delivery.model.enums.OrderStatus;

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
