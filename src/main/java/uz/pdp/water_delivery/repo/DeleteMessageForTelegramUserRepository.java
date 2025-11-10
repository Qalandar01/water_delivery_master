package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.entity.deletMessage.DeleteMessageForTelegramUser;

import java.util.List;
import java.util.UUID;

public interface DeleteMessageForTelegramUserRepository extends JpaRepository<DeleteMessageForTelegramUser, Long> {

    void deleteByChatIdAndMessage(Long chatId, String message);

    List<DeleteMessageForTelegramUser> findAllByChatId(Long chatId);

    void deleteAllByChatId(Long chatId);
}