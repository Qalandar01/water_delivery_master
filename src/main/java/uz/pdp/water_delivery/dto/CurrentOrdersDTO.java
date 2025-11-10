package uz.pdp.water_delivery.dto;

import java.util.UUID;

public record CurrentOrdersDTO(
        Long orderId,
        Location location,
        Long courierId
) {



}
