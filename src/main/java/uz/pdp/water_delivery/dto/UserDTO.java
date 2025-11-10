package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.water_delivery.entity.District;

import java.util.List;
import java.util.UUID;

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
    private List<District> districts;
}
