package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;

import java.util.List;
import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

    Optional<TelegramUser> findByChatId(Long chatId);

    List<SimpleWaitingUser> findAllByStateInOrderByCreatedAt(List<TelegramState> states);


    @Query(value = "SELECT * FROM telegram_user t WHERE t.user_id = (SELECT u.id FROM users u WHERE u.phone = :keyword)", nativeQuery = true)
    List<TelegramUser> searchByUserPhone(@Param("keyword") String keyword);



    @Query(value = "SELECT * FROM telegram_user WHERE phone_off = true", nativeQuery = true)
    List<TelegramUser> findAllByPhoneOff();


    @Query(value = "SELECT tu.* FROM telegram_user tu " +
            "JOIN users u ON tu.user_id = u.id " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.role_name = 'ROLE_USER'", nativeQuery = true)
    List<TelegramUser> findAllByRoleNameUsers();

    Optional<TelegramUser> findByUser(User user);

    List<SimpleWaitingUser> findAllByChangeLocation(boolean b);

    @Query(value = "SELECT COUNT(*) FROM telegram_user tu WHERE tu.change_location = :b", nativeQuery = true)
    Integer countByChangeLocation(@Param("b") boolean b);

}