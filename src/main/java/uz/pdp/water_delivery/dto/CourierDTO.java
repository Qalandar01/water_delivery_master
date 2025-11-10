package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.District;

import java.util.List;

public class CourierDTO {
    private Courier courier;
    private List<District> districts;
    private boolean hasOrders;

    public CourierDTO(Courier courier, List<District> districts, boolean hasOrders) {
        this.courier = courier;
        this.districts = districts;
        this.hasOrders = hasOrders;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    public boolean isHasOrders() {
        return hasOrders;
    }

    public void setHasOrders(boolean hasOrders) {
        this.hasOrders = hasOrders;
    }
}
