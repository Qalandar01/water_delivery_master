package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.water_delivery.dto.BottleTypeCountDTO;
import uz.pdp.water_delivery.entity.enums.CourierStatus;
import uz.pdp.water_delivery.repo.BottleTypesRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "courier")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    private String carType;

    private String carNumber;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private CourierStatus courierStatus;

    private Boolean isActive = true;

    @OneToOne
    private User user;

    @ManyToMany
    private List<District> districts;

    @Transient
    private List<BottleTypeCountDTO> bottleCount;

    @Override
    public String toString() {
        String districtNames = districts != null ? districts.stream()
                .map(District::getName)
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