package uz.pdp.water_delivery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperatorDTO {
    private Long id;
    private String firstName;
    private String displayPhone;
    private boolean active;
}
