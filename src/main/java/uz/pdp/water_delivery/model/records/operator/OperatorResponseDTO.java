package uz.pdp.water_delivery.model.records.operator;

public record OperatorResponseDTO(
        Long id,
        String firstName,
        String displayPhone,
        boolean active
) {}
