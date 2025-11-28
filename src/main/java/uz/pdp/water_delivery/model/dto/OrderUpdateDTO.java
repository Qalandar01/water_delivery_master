package uz.pdp.water_delivery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateDTO {
    private Long orderId;
    private LocalDate orderTime;
    private String orderStatus;
    private Integer deliveryTimeId;
}
