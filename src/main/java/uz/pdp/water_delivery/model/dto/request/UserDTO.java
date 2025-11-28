package uz.pdp.water_delivery.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+]{9,15}$",
            message = "Phone number must contain only digits or '+' and be 9–15 characters long"
    )
    private String phone;

    @NotBlank(message = "Car type is required")
    private String carType;

    @NotBlank(message = "Car number is required")
    @Pattern(
            regexp = "^[A-Z0-9-]{5,10}$",
            message = "Car number format is invalid"
    )
    private String carNumber;

    private boolean isActive;
//
//    @NotBlank(message = "Courier status is required")
//    private String courierStatus;
//
//    @NotEmpty(message = "Districts list cannot be empty")
//    private List<@NotBlank(message = "District name cannot be blank") String> districts;
}
