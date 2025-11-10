package uz.pdp.water_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyUserDTO {

    private Long tgUserId;
    private Double longitude;
    private Double latitude;
    private Long districtId;
    private String addressLine;
    private String phone;
    private boolean isHome = false;
    private String xonadon;
    private String podyez;
    private String qavat;
    private String kvRaqami;

}
