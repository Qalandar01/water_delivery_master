package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.dto.Location;
import uz.pdp.water_delivery.entity.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

    @Id
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private TelegramUser telegramUser;

    private LocalDate day;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    @ManyToOne
    private DeliveryTime deliveryTime;

    @Embedded
    private Location location;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String phone;

    private Double totalPrice;

    @ManyToOne
    private Courier courier;

    @ManyToOne
    private Region region;

    @ManyToOne
    private District district;

}
