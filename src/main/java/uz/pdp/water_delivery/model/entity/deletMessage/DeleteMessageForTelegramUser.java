package uz.pdp.water_delivery.entity.deletMessage;

import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    @Column(length = 1000)
    private String message;

    private Integer messageId;



}