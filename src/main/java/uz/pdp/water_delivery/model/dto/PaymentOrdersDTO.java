package uz.pdp.water_delivery.model.dto;

import java.util.List;

public record PaymentOrdersDTO(
        List<OrderSummaryDTO> orderSummary
) {}
