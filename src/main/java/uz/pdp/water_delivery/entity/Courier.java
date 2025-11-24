package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.water_delivery.dto.BottleTypeCountDTO;
import uz.pdp.water_delivery.entity.enums.CourierStatus;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "courier")
@SQLRestriction("is_deleted=false")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String carType;

    private String carNumber;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private CourierStatus courierStatus;

    private Boolean isActive = true;

    private Boolean isDeleted = false;

    @OneToOne
    private User user;

    private List<String> districts;

    @Transient
    private List<BottleTypeCountDTO> bottleCount;

    @Override
    public String toString() {
        String districtNames = districts != null ? districts.stream()
                .reduce((name1, name2) -> name1 + ", " + name2)
                .orElse("No districts") : "No districts";

        return " Tumanlar: " + districtNames + "\n Kuryer statusi: " + courierStatus;
    }

    public String getFullName() {
        return String.format(
                "🚗 Avto turi: %s<br>" +
                        "🚘 Raqami: %s<br>" +
                        "✅ Sig'imi: %s",
                carType != null ? carType : "N/A",
                carNumber != null ? carNumber : "N/A",
                capacity != null ? capacity : "N/A"
        );
    }






}