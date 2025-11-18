package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import uz.pdp.water_delivery.entity.Courier;

import java.util.List;
@Data
@AllArgsConstructor
public class OrdersPageData {
    private List<Courier> couriers;
    private Courier currentCourier;
    private List<CurrentOrdersDTO> currentOrders;
    private CompanyDTO company;
    private List<OrderDto> orders;
}
