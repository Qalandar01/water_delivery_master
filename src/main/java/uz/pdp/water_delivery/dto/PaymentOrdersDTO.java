package uz.pdp.water_delivery.dto;

import java.util.List;

public record PaymentOrdersDTO(
        List<OrderSummaryDTO> orderSummary
) {}
