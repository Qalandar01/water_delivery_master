package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.entity.BottleTypes;
import uz.pdp.water_delivery.entity.Order;
import uz.pdp.water_delivery.entity.OrderProduct;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrderId(Long orderId);

    long countByBottleTypes (BottleTypes bottleType);

    List<OrderProduct> findAllByOrder(Order order);

    List<OrderProduct> findAllByOrderIn(List<Order> orders);
}
