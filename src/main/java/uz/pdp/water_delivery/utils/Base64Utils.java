package uz.pdp.water_delivery.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.entity.BottleTypes;

import java.util.Base64;

@Data
@AllArgsConstructor
@Service
public class Base64Utils {
    public String getBase64Image(BottleTypes bottleTypes) {
        return Base64.getEncoder().encodeToString(bottleTypes.getImage());
    }

}
