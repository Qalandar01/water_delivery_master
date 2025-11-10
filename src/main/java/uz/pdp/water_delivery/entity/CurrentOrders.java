package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "current_orders")
public class CurrentOrders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private Integer orderCount;

    private LocalDateTime waitingTime;


    public CurrentOrders(Order order, Integer orderCount) {
        this.order = order;
        this.orderCount = orderCount;
    }
}
