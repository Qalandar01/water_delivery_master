package uz.pdp.water_delivery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.model.records.CompanyDTO;
import uz.pdp.water_delivery.model.records.CurrentOrdersDTO;
import uz.pdp.water_delivery.model.records.OrderResponseDTO;

import java.util.List;
@Data
@AllArgsConstructor
public class OrdersPageData {
    private List<Courier> couriers;
    private Courier currentCourier;
    private List<CurrentOrdersDTO> currentOrders;
    private CompanyDTO company;
    private List<OrderResponseDTO> orders;
}
