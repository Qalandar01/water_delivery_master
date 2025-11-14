package uz.pdp.water_delivery.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GiftWaterRequest {
    private Long bottleTypeId;
    private Integer saleAmount;
    private Integer saleDiscount;
    private Boolean saleActive;
    private LocalDate saleStartTime;
    private LocalDate saleEndTime;
}
