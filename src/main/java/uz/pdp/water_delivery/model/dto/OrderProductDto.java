package uz.pdp.water_delivery.model.dto;

import uz.pdp.water_delivery.model.entity.OrderProduct;

import java.util.ArrayList;
import java.util.List;

public record OrderProductDto(
        String name,
        int amount
) {


    public static List<OrderProductDto> makeListFromEntity(List<OrderProduct> list) {
        List<OrderProductDto> dtos = new ArrayList<>();
        for (OrderProduct product : list) {
            dtos.add(new OrderProductDto(product.getProduct().getType(), product.getAmount()));
        }
        return dtos;
    }
}
