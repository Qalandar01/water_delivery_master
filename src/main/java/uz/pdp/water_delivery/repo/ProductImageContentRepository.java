package uz.pdp.water_delivery.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.water_delivery.model.entity.ProductImageContent;

import java.util.List;

@Repository
public interface ProductImageContentRepository extends JpaRepository<ProductImageContent, Long> {
    List<ProductImageContent> findByProductImage_Id(Long productImageId);
}
