package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product")
@SQLRestriction("is_deleted=false")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String type;

    @Column(length = 512)
    private String description;

    private Integer price;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductImage productImage;

    private Long orderCount;

    @Column(nullable = false)
    private boolean isReturnable = false;

    private Boolean isDeleted = false;

    private Integer sale_amount;

    private Integer sale_discount;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean sale_active = false;


    private LocalDate sale_startDate;

    private LocalDate sale_endDate;


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product that = (Product) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", active=" + active +
                ", orderCount=" + orderCount +
                ", isReturnable=" + isReturnable +
                ", sale_amount=" + sale_amount +
                ", sale_discount=" + sale_discount +
                ", sale_active=" + sale_active +
                ", sale_startDate=" + sale_startDate +
                ", sale_endDate=" + sale_endDate +
                '}';
    }
}
