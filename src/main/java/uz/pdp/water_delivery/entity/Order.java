package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
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
@SQLRestriction("is_deleted=false")
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

    private Boolean isDeleted = false;

    private Double totalPrice;

    @ManyToOne
    private Courier courier;

    private String region;

    private String district;

}
