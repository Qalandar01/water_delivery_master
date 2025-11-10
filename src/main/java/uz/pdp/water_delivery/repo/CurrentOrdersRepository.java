package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.entity.CurrentOrders;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurrentOrdersRepository extends JpaRepository<CurrentOrders, UUID> {

    @Query("SELECT co FROM CurrentOrders co " +
            "JOIN co.order o " +
            "JOIN o.courier c " +
            "WHERE o.courier.id = :courierId " +
            "AND o.orderStatus = :orderStatus " +
            "OR o.orderStatus = :waitingPhone " +
            "ORDER BY co.orderCount asc")
    List<CurrentOrders> findSortedOrders(@Param("courierId") UUID courierId,
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
    Optional<Integer> getLastCount(@Param("courierId") UUID courierId,
                                         @Param("orderStatus") OrderStatus orderStatus, OrderStatus waitingPhone);


}