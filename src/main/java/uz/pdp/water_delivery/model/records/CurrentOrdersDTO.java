package uz.pdp.water_delivery.model.records;

import uz.pdp.water_delivery.model.dto.Location;

public record CurrentOrdersDTO(
        Long orderId,
        Location location,
        Long courierId
) {



}
