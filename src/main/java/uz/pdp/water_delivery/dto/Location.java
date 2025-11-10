package uz.pdp.water_delivery.dto;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class  Location {

    private Double latitude;
    private Double longitude;

    @Override
    public String toString() {
        return latitude+","+longitude;
    }
}
