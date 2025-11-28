package uz.pdp.water_delivery.model.records.courier;

import uz.pdp.water_delivery.model.entity.Courier;
import java.util.List;

public record CourierResponseDTO(
        Courier courier,
        List<String> districts,
        boolean hasOrders
) {}
