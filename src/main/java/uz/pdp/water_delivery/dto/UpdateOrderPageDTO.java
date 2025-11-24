package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.DeliveryTime;
import uz.pdp.water_delivery.entity.Order;
import uz.pdp.water_delivery.entity.OrderProduct;

import java.util.List;

public class UpdateOrderPageDTO {
    private final Order order;
    private final List<OrderProduct> orderProducts;
    private final List<DeliveryTime> availableTimes;
    private final Long selectedTimeId;

    public UpdateOrderPageDTO(Order order, List<OrderProduct> orderProducts,
                              List<DeliveryTime> availableTimes, Long selectedTimeId) {
        this.order = order;
        this.orderProducts = orderProducts;
        this.availableTimes = availableTimes;
        this.selectedTimeId = selectedTimeId;
    }

    // Getters
    public Order getOrder() { return order; }
    public List<OrderProduct> getOrderProducts() { return orderProducts; }
    public List<DeliveryTime> getAvailableTimes() { return availableTimes; }
    public Long getSelectedTimeId() { return selectedTimeId; }
}
