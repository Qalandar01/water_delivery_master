package uz.pdp.water_delivery.utils;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.repo.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledUtils {

    private final UserRepository userRepository;

    public ScheduledUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Scheduled(cron = "0 30 12,18,23 * * *")
//    public void autoChangeStateOrders(){
//        List<DeliveryTime> deliveryTimes = deliveryTimeRepository.findAllByDayOrderByIdAsc("Bugun");
//        List<Order> orders = orderRepository.findAllByOrderStatusAndDay(OrderStatus.ASSIGNED, LocalDate.now());
//        LocalTime now = LocalTime.now();
//        for (Order order : orders) {
//            DeliveryTime deliveryTime = order.getDeliveryTime();
//            if (deliveryTimes.contains(deliveryTime)) {
//                LocalTime endTime = deliveryTime.getEndTime();
//                if (endTime.plusMinutes(30).isBefore(now)) {
//                    order.setOrderStatus(OrderStatus.END_TIME);
//                    orderRepository.save(order);
//                }
//            }
//        }
//    }

//    @Scheduled(cron = "0 10 0 * * *")
//    public void autoChangeDeliveryTime() {
//        List<DeliveryTime> bugunDeliveryTimes = deliveryTimeRepository.findAllByDayOrderByIdAsc("Bugun");
//        List<DeliveryTime> ertagaDeliveryTimes = deliveryTimeRepository.findAllByDayOrderByIdAsc("Ertaga");
//        for (DeliveryTime ertagaTime : ertagaDeliveryTimes) {
//            List<Order> ertagaOrders = orderRepository.findAllByDeliveryTime(ertagaTime);
//            DeliveryTime matchingBugunTime = bugunDeliveryTimes.stream()
//                    .filter(bugunTime -> bugunTime.getStartTime().equals(ertagaTime.getStartTime())
//                            && bugunTime.getEndTime().equals(ertagaTime.getEndTime()))
//                    .findFirst()
//                    .orElse(null);
//            if (matchingBugunTime != null) {
//                for (Order order : ertagaOrders) {
//                    order.setDeliveryTime(matchingBugunTime);
//                    orderRepository.save(order);
//                }
//            }
//        }
//    }

    @Scheduled(cron = "0 0 0 1 * *") // Har oyning 1 kuni soat 00:00 da ishga tushadi
    @Transactional
    public void checkUserPayments() {
        LocalDate currentDate = LocalDate.now();
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            if (user.getPaidDate() == null || user.getPaidDate().isBefore(currentDate.minusMonths(1))) {
                user.setActive(false);
            } else {
                user.setActive(true);
            }
        });
        userRepository.saveAll(users);
    }

}
