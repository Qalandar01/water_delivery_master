package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.entity.Product;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrderId(Long orderId);

    long countByProduct (Product product);

    List<OrderProduct> findAllByOrder(Order order);

    List<OrderProduct> findAllByOrderIn(List<Order> orders);
}
