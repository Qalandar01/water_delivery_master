package uz.pdp.water_delivery.entity.deletMessage;

import com.pengrad.telegrambot.response.SendResponse;
import jakarta.persistence.*;
import lombok.*;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.repo.DeleteMessageForTelegramUserRepository;

import java.util.UUID;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Entity
@Table(name = "deleting_messages")
public class DeleteMessageForTelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long chatId;

    @Column(length = 1000)
    private String message;

    private Integer messageId;



}