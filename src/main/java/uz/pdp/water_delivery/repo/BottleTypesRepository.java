package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.water_delivery.dto.BottleTypeCountDTO;
import uz.pdp.water_delivery.entity.BottleTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BottleTypesRepository extends JpaRepository<BottleTypes, Integer> {
    List<BottleTypes> findAllByActiveTrue();

    Optional<BottleTypes> findByType(String type);

    boolean existsByType(String type);

    @Query(value = "SELECT " +
            "    bt.type as type, sum(op.amount) AS totalCount " +
            "FROM orders o " +
            "         INNER JOIN order_product op ON o.id = op.order_id " +
            "         INNER JOIN bottle_types bt ON op.bottle_types_id = bt.id " +
            "         INNER JOIN courier c ON o.courier_id = c.id " +
            "WHERE o.order_status = 'ASSIGNED' " +
            "  AND c.id = ?1 " +
            "  AND o.day = ?2 " +
            "GROUP BY bt.type " +
            "ORDER BY MAX(bt.id) DESC",
            nativeQuery = true)
    List<Object[]> countOrdersByCourier(UUID courierId, LocalDate day);

}