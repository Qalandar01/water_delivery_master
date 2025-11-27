package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    @Query(value = "SELECT * FROM courier WHERE is_active = :isActive", nativeQuery = true)
    List<Courier> findAllByIsActive(boolean isActive);

    boolean existsByUserPhoneOrIdNot(String phone, Long id);
    Courier findByUserId(Long id);

    boolean existsByUserPhoneAndIdNot(String repairedPhone, Long id);

    @Query(value = "SELECT DISTINCT c.* " +
            "FROM courier c " +
            "INNER JOIN orders o ON c.id = o.courier_id " +
            "INNER JOIN delivery_time dt ON o.delivery_time_id = dt.id " +
            "WHERE dt.day = :today " +
            "AND c.is_active = true", nativeQuery = true)
    List<Courier> findAllByCouriers(@Param("today") String today);

    Optional<Courier> findByUser(User user);
}