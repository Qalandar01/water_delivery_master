package uz.pdp.water_delivery.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.OrderProduct;
import uz.pdp.water_delivery.model.entity.Product;

import java.util.Collection;
import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrderId(Long orderId);

    long countByProduct (Product product);

    List<OrderProduct> findAllByOrder(Order order);

    List<OrderProduct> findAllByOrderIn(List<Order> orders);

    @Query(value = """
                SELECT product_id, COUNT(*)
                FROM order_product
                GROUP BY product_id
            """, nativeQuery = true)
    List<Object[]> findOrderCountGroupedByProductNative();


    List<OrderProduct> findAllByOrderId(Long orderId);
}
