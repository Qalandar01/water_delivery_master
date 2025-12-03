package uz.pdp.water_delivery.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDTO {

    private Long id;

    @NotBlank(message = "Type cannot be empty")
    @Size(min = 2, max = 50, message = "Type must be between 2 and 50 characters")
    private String type;

    private boolean active;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 5, max = 200, message = "Description must be between 5 and 200 characters")
    private String description;

    @NotNull(message = "Image file is required")
    private MultipartFile image;

    @NotNull(message = "Price must not be null")
    @Positive(message = "Price must be a positive number")
    private Long price;

    private boolean isReturnable;
}
