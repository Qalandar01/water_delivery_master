package uz.pdp.water_delivery.dto;

import uz.pdp.water_delivery.entity.OrderProduct;

import java.util.ArrayList;
import java.util.List;

public record OrderProductDto(
        String name,
        int amount
) {


    public static List<OrderProductDto> makeListFromEntity(List<OrderProduct> list) {
        List<OrderProductDto> dtos = new ArrayList<>();
        for (OrderProduct product : list) {
            dtos.add(new OrderProductDto(product.getBottleTypes().getType(), product.getAmount()));
        }
        return dtos;
    }
}
