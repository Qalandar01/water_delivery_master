package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;

    @ManyToOne
    private Order order;

    @ManyToOne
    private BottleTypes bottleTypes;

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
        this.priceAtPurchase = bottleTypes.getPrice();
        this.discountAtPurchase = isOnSale() ? bottleTypes.getSale_amount() : null;
    }

    /**
     * Mahsulotning umumiy narxini hisoblaydi
     * @return total price (so'mda)
     */
    public Integer getTotalPrice() {
        if (isOnSale()) {
            return (amount - bottleTypes.getSale_amount()) * bottleTypes.getPrice();
        } else {
            return amount * bottleTypes.getPrice();
        }
    }

    /**
     * Chegirma mavjudligini tekshiradi
     * @return true agar chegirma faollashtirilgan bo'lsa
     */
    public boolean isOnSale() {
        return bottleTypes.getSale_active() &&
                bottleTypes.getSale_startDate() != null &&
                bottleTypes.getSale_endDate() != null &&
                !LocalDate.now().isBefore(bottleTypes.getSale_startDate()) &&
                !LocalDate.now().isAfter(bottleTypes.getSale_endDate());
    }

    /**
     * Chegirma narxini hisoblaydi
     * @return chegirmadan keyingi narx
     */
}
