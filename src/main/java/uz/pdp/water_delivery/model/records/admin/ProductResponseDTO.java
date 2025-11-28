package uz.pdp.water_delivery.model.records.admin;

import java.time.LocalDate;

public record ProductResponseDTO(
        Long id,
        String type,
        String description,
        Integer price,
        Boolean active,
        Long orderCount,
        Boolean returnable,
        Integer saleAmount,
        Integer saleDiscount,
        Boolean saleActive,
        LocalDate saleStartDate,
        LocalDate saleEndDate
) {}
