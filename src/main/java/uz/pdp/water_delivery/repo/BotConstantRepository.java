package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.water_delivery.model.entity.BotConstant;

import java.util.Optional;

@Repository
public interface BotConstantRepository extends JpaRepository<BotConstant, Long> {

    Optional<BotConstant> findByConstantKey(String key);
}
