package uz.pdp.water_delivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderSummaryDTO {
    private String fullName;
    private String carType;
    private UUID courierId;
    private Integer deliveryTimeId;
    private Long orderSize;
    private Long completedOrders;
    private Long notAnswered;
    private Long notInTime;
    private Long bottlesCount;

    public OrderSummaryDTO(String fullName, String carType, UUID courierId, Integer deliveryTimeId, Long orderSize, Long completedOrders, Long notAnswered, Long notInTime, Long bottlesCount) {
        this.fullName = fullName;
        this.carType = carType;
        this.courierId = courierId;
        this.deliveryTimeId = deliveryTimeId;
        this.orderSize = orderSize;
        this.completedOrders = completedOrders;
        this.notAnswered = notAnswered;
        this.notInTime = notInTime;
        this.bottlesCount = bottlesCount;
    }

}
