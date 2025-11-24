package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@SQLRestriction("is_deleted=false")
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;

    @ManyToOne
    private Order order;

    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer bottleCount;

    private Integer priceAtPurchase; // Sotib olingan paytdagi narx

    private Integer discountAtPurchase; // Sotib olingan paytdagi chegirma

    private Boolean wasOnSale; // Aksiya borligi

    /**
     * Sotib olish vaqtidagi narx va chegirmalarni saqlash
     */
    @PrePersist
    public void capturePricingDetails() {
        this.wasOnSale = isOnSale();
        this.priceAtPurchase = product.getPrice();
        this.discountAtPurchase = isOnSale() ? product.getSale_amount() : null;
    }

    /**
     * Mahsulotning umumiy narxini hisoblaydi
     * @return total price (so'mda)
     */
    public Integer getTotalPrice() {
        if (isOnSale()) {
            return (amount - product.getSale_amount()) * product.getPrice();
        } else {
            return amount * product.getPrice();
        }
    }

    /**
     * Chegirma mavjudligini tekshiradi
     * @return true agar chegirma faollashtirilgan bo'lsa
     */
    public boolean isOnSale() {
        return product.getSale_active() &&
                product.getSale_startDate() != null &&
                product.getSale_endDate() != null &&
                !LocalDate.now().isBefore(product.getSale_startDate()) &&
                !LocalDate.now().isAfter(product.getSale_endDate());
    }

    /**
     * Chegirma narxini hisoblaydi
     * @return chegirmadan keyingi narx
     */
}
