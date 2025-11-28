package uz.pdp.water_delivery.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bot_constants")
public class BotConstant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "constant_key", nullable = false, unique = true)
    private String constantKey;

    @Column(name = "constant_value", nullable = false, columnDefinition = "TEXT")
    private String constantValue;

}
