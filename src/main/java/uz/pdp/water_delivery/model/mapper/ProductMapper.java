package uz.pdp.water_delivery.model.mapper;

import uz.pdp.water_delivery.model.dto.request.ProductRequestDTO;
import uz.pdp.water_delivery.model.entity.Product;
import uz.pdp.water_delivery.model.records.admin.ProductResponseDTO;

public class ProductMapper {

    public static ProductResponseDTO mapToResponse(Product product) {


        return new ProductResponseDTO(
                product.getId(),
                product.getType(),
                product.getDescription(),
                product.getPrice(),
                product.getActive(),
                product.getOrderCount(),
                product.getReturnable(),
                product.getSale_amount(),
                product.getSale_discount(),
                product.getSale_active(),
                product.getSale_startDate(),
                product.getSale_endDate()
        );
    }
    public static ProductRequestDTO mapToRequest(Product entity) {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setActive(entity.getActive());
        dto.setReturnable(entity.getReturnable());
        return dto;
    }
}
