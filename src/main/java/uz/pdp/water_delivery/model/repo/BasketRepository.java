package uz.pdp.water_delivery.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.entity.Basket;
import uz.pdp.water_delivery.model.entity.Product;

import java.util.List;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    List<Basket> findAllByTelegramUser(TelegramUser telegramUser);

    Basket findByTelegramUserAndProduct(TelegramUser telegramUser, Product product);

    void deleteAllByTelegramUser(TelegramUser tgUser);

}
