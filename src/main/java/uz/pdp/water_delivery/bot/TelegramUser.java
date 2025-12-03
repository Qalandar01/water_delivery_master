package uz.pdp.water_delivery.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.water_delivery.entity.abs.AbsEntity;
import uz.pdp.water_delivery.model.dto.Location;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.enums.TelegramState;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "telegram_user")
@SQLRestriction("is_deleted=false")
public class TelegramUser extends AbsEntity {

    private Long chatId;

    @Enumerated(EnumType.STRING)
    private TelegramState state = TelegramState.START;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    private Integer getDeletingMessage;

    @Embedded
    private Location location;

    private String region;

    private String district;

    private Boolean isHome = false;

    private Boolean isDeleted = false;

    private String xonadon;

    private String podyez;

    private String qavat;

    private String kvRaqami;

    private String password;



    private Integer deletingMessage;

    private Boolean verified = false;

    private String addressLine;

    private Integer productCount = 1;

    private Integer editingMessageId;

    private Integer orderCount = 1;

    private Integer currentOrderCount = 0;

    private Boolean phoneOff = false;

    private Boolean changeLocation = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private LocalDate currentOrderDay;

    private Long currentOrderId;

    private Integer editingCurrentMessageId;

    public TelegramUser(Long chatId) {
        this.chatId = chatId;
    }

    public Long generateOrderId() {
        return Long.parseLong(chatId + "" + orderCount++);
    }

    public String calcTotalAmountOfCurrentOrder() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE);
        return numberFormat.format((long) product.getPrice() * productCount);
    }



    public void deleteMessage(TelegramBot telegramBot, Integer messageId) {
        if (messageId != null) {
            telegramBot.execute(new DeleteMessage(chatId, messageId));
        }
    }

    public void deleteMessage(TelegramBot telegramBot, Integer messageId, Integer deletingMessage) {
        if (messageId != null && deletingMessage != null) {
            telegramBot.execute(new DeleteMessage(chatId, messageId));
            telegramBot.execute(new DeleteMessage(chatId, deletingMessage));
        }
    }
}