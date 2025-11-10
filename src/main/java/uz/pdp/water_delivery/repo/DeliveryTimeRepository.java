package uz.pdp.water_delivery.repo;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.entity.DeliveryTime;

import java.util.List;

public interface DeliveryTimeRepository extends JpaRepository<DeliveryTime, Long> {

    List<DeliveryTime> findAllByDayOrderByIdAsc(String bugun);

    List<DeliveryTime> findAllByOrderByIdAsc();
}