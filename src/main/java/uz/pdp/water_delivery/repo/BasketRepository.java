package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.Basket;
import uz.pdp.water_delivery.entity.BottleTypes;

import java.util.List;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    List<Basket> findAllByTelegramUser(TelegramUser telegramUser);

    Basket findByTelegramUserAndBottleType(TelegramUser telegramUser, BottleTypes bottleTypes);

    void deleteAllByTelegramUser(TelegramUser tgUser);

}
