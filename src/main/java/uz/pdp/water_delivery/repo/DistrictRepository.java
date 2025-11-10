package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.entity.District;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {


    @Query(value = """
        SELECT * FROM district
        WHERE LOWER(SUBSTRING(name, 1, 5)) = LOWER(SUBSTRING(:districtName, 1, 5))
        LIMIT 1;
""", nativeQuery = true)
    District findBySimilarName(@Param("districtName") String districtName);



    @Query(value = "SELECT * FROM  district d", nativeQuery = true)
    List<District> getAllDistricts();

    District findByName(String name);
}