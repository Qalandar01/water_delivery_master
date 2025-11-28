package uz.pdp.water_delivery.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;
import uz.pdp.water_delivery.model.dto.ProductCountDTO;
import uz.pdp.water_delivery.model.enums.CourierStatus;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
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
    private List<ProductCountDTO> productCount;

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


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Courier courier = (Courier) o;
        return getId() != null && Objects.equals(getId(), courier.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}