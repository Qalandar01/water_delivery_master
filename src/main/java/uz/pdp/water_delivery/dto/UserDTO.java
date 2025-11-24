package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String carType;
    private String carNumber;
    private String courierStatus;
    private List<String> districts;
}
