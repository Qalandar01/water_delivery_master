package uz.pdp.water_delivery.model.dto.response;

import lombok.Getter;
import lombok.Setter;
import uz.pdp.water_delivery.model.entity.Courier;

import java.util.List;

@Setter
@Getter
public class CourierDTO {
    private Courier courier;
    private List<String> districts;
    private boolean hasOrders;

    public CourierDTO(Courier courier, List<String> districts, boolean hasOrders) {
        this.courier = courier;
        this.districts = districts;
        this.hasOrders = hasOrders;
    }

}
