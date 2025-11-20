package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String type;
    private boolean active;
    private String description;
    private MultipartFile image;
    private Integer price;
    private boolean isReturnable;
}
