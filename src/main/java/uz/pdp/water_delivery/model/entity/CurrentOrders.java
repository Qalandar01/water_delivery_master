package uz.pdp.water_delivery.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "current_orders")
@SQLRestriction("is_deleted=false")
public class CurrentOrders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private Integer orderCount;

    private LocalDateTime waitingTime;

    private Boolean isDeleted = false;

    public CurrentOrders(Order order, Integer orderCount) {
        this.order = order;
        this.orderCount = orderCount;
    }
}
