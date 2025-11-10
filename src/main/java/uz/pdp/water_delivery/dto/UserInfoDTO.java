package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.water_delivery.entity.Region;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {

    private Location location;
    private String phone;
    private Region region;

}
