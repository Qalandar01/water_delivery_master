package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.Order;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderStatus(OrderStatus orderStatus);

    boolean existsByCourierAndOrderStatus(Courier courier, OrderStatus orderStatus);


    boolean existsByCourier(Courier courier);

    List<Order> findAllByCourierIdAndOrderStatus(Long id, OrderStatus orderStatus);


    @Query(value = """
            SELECT
                u.first_name || ' ' || u.last_name AS fullName,
                c.car_type,
                c.id,
                o.delivery_time_id,
                COUNT(DISTINCT o.id) AS order_size,
                SUM(CASE WHEN o.order_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrders,
                SUM(CASE WHEN o.order_status = 'PHONE_OFF' THEN 1 ELSE 0 END) AS notAnswered,
                SUM(CASE WHEN o.order_status = 'END_TIME' THEN 1 ELSE 0 END) AS notInTime,
                SUM(COALESCE(oP.amount, 0)) AS totalBottles
            FROM
                orders o
                    INNER JOIN
                courier c ON c.id = o.courier_id
                    INNER JOIN
                users u ON u.id = c.user_id
                    INNER JOIN
                order_product oP ON o.id = oP.order_id
            WHERE
                o.day = CURRENT_DATE
            GROUP BY
                u.id, c.id, o.delivery_time_id;
            """,
            nativeQuery = true)
    List<Object[]> countOrdersByCourierAndOrderStatus();


    List<Order> findByTelegramUserAndOrderStatus(TelegramUser tgUser, OrderStatus orderStatus);

    List<Order> findAllByTelegramUser(TelegramUser telegramUser);


    boolean existsByTelegramUser(TelegramUser telegramUser);




}