package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.entity.Region;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    Region findByName(String toshkent);
}