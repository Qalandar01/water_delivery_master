package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.water_delivery.bot.TelegramUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "baskets")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TelegramUser telegramUser;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer amount;

    private Integer messageId;


    public Double getTotalPrice() {
        return (double) (amount * product.getPrice());
    }
}
