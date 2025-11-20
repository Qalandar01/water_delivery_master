package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductEditView {
    private ProductDTO dto;
    private String base64Image;
}
