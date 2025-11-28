package uz.pdp.water_delivery.model.dto.request.operator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateOperatorRequestDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "\\+\\d{12}", message = "Phone must be in the format +998XXXXXXXXX")
    private String phone;

    private boolean active;
    private boolean paid;

}
