package uz.pdp.water_delivery.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_image_content")
public class ProductImageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private byte[] content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_image_id", nullable = false)
    private ProductImage productImage;
}
