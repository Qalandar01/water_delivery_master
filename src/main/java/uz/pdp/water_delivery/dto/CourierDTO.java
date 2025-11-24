package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.Courier;

import java.util.List;

public class CourierDTO {
    private Courier courier;
    private List<String> districts;
    private boolean hasOrders;

    public CourierDTO(Courier courier, List<String> districts, boolean hasOrders) {
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

    public List<String> getDistricts() {
        return districts;
    }

    public void setDistricts(List<String> districts) {
        this.districts = districts;
    }

    public boolean isHasOrders() {
        return hasOrders;
    }

    public void setHasOrders(boolean hasOrders) {
        this.hasOrders = hasOrders;
    }
}
