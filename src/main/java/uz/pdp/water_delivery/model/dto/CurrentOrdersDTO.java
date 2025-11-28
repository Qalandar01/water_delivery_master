package uz.pdp.water_delivery.model.dto;

public record CurrentOrdersDTO(
        Long orderId,
        Location location,
        Long courierId
) {



}
