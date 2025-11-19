package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.DeliveryTime;
import uz.pdp.water_delivery.entity.Order;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderStatus(OrderStatus orderStatus);

    boolean existsByCourierAndOrderStatus(Courier courier, OrderStatus orderStatus);

    List<Order> findAllByOrderStatusIn(List<OrderStatus> list);

    boolean existsByCourier(Courier courier);

    List<Order> findAllByCourierIdAndOrderStatusAndDeliveryTime(Long id, OrderStatus orderStatus, DeliveryTime currentOrderDeliveryTime);

    List<Order> findAllByCourierIdAndOrderStatusInAndDeliveryTime(Long courier_id, Collection<OrderStatus> orderStatus, DeliveryTime deliveryTime);

    List<Order> findAllByOrderStatusAndDay(OrderStatus orderStatus, LocalDate selectedDay);

    List<Order> findAllByOrderStatusAndDeliveryTimeIdAndDay(OrderStatus orderStatus, Long id, LocalDate selectedDay);

    @Query(value = "SELECT\n" +
            "    u.first_name || ' ' || u.last_name AS fullName,\n" +
            "    c.car_type,\n" +
            "    c.id,\n" +
            "    o.delivery_time_id,\n" +
            "    COUNT(DISTINCT o.id) AS order_size,\n" +
            "    SUM(CASE WHEN o.order_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrders,\n" +
            "    SUM(CASE WHEN o.order_status = 'PHONE_OFF' THEN 1 ELSE 0 END) AS notAnswered,\n" +
            "    SUM(CASE WHEN o.order_status = 'END_TIME' THEN 1 ELSE 0 END) AS notInTime,\n" +
            "    SUM(COALESCE(oP.amount, 0)) AS totalBottles\n" +
            "FROM\n" +
            "    orders o\n" +
            "        INNER JOIN\n" +
            "    courier c ON c.id = o.courier_id\n" +
            "        INNER JOIN\n" +
            "    users u ON u.id = c.user_id\n" +
            "        INNER JOIN\n" +
            "    order_product oP ON o.id = oP.order_id\n" +
            "WHERE\n" +
            "    o.day = CURRENT_DATE\n" +
            "GROUP BY\n" +
            "    u.id, c.id, o.delivery_time_id;\n",
            nativeQuery = true)
    List<Object[]> countOrdersByCourierAndOrderStatus();


    List<Order> findAllByDeliveryTime(DeliveryTime ertagaTime);

    List<Order> findByTelegramUserAndOrderStatus(TelegramUser tgUser, OrderStatus orderStatus);

    List<Order> findAllByTelegramUser(TelegramUser telegramUser);

    void deleteByCourier(Courier courier);


    boolean existsByTelegramUser(TelegramUser telegramUser);


    List<Order> findByCourier(Courier courier);


}