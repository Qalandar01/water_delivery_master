package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.DeliveryTime;

import java.util.List;

public record PaymentOrdersDTO(
        List<DeliveryTime> deliveryTimes,
        List<OrderSummaryDTO> orderSummary
) {}
