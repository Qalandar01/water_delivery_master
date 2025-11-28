package uz.pdp.water_delivery.model.dto;

import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;

import java.util.List;

public class UpdateOrderPageDTO {
    private final Order order;
    private final List<OrderProduct> orderProducts;

    public UpdateOrderPageDTO(Order order, List<OrderProduct> orderProducts) {
        this.order = order;
        this.orderProducts = orderProducts;
    }

    // Getters
    public Order getOrder() { return order; }
    public List<OrderProduct> getOrderProducts() { return orderProducts; }
}
