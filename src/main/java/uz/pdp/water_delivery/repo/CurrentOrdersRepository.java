package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.model.entity.CurrentOrders;
import uz.pdp.water_delivery.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface CurrentOrdersRepository extends JpaRepository<CurrentOrders, Long> {

    @Query("SELECT co FROM CurrentOrders co " +
            "JOIN co.order o " +
            "JOIN o.courier c " +
            "WHERE o.courier.id = :courierId " +
            "AND o.orderStatus = :orderStatus " +
            "OR o.orderStatus = :waitingPhone " +
            "ORDER BY co.orderCount asc")
    List<CurrentOrders> findSortedOrders(@Param("courierId") Long courierId,
                                         @Param("orderStatus") OrderStatus orderStatus, OrderStatus waitingPhone);


    @Query("SELECT co FROM CurrentOrders co " +
            "JOIN co.order o " +
            "JOIN o.courier c " +
            "WHERE "+
            "o.orderStatus = :orderStatus " +
            "OR o.orderStatus = :waitingPhone " +
            "ORDER BY co.orderCount asc")
    List<CurrentOrders> findSortedOrders(
                                         @Param("orderStatus") OrderStatus orderStatus, OrderStatus waitingPhone);



    Optional<CurrentOrders> findByOrderId(Long orderId);

    @Query("SELECT co.orderCount FROM CurrentOrders co " +
            "JOIN co.order o " +
            "JOIN o.courier c " +
            "WHERE o.courier.id = :courierId " +
            "AND o.orderStatus = :orderStatus " +
            "OR o.orderStatus = :waitingPhone " +
            "ORDER BY co.orderCount desc limit 1")
    Optional<Integer> getLastCount(@Param("courierId") Long courierId,
                                         @Param("orderStatus") OrderStatus orderStatus, OrderStatus waitingPhone);


}