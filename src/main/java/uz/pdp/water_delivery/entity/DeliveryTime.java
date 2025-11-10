package uz.pdp.water_delivery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeliveryTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String day;

    private LocalTime startTime;

    private LocalTime endTime;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return startTime.format(formatter) + " - " + endTime.format(formatter);
    }

    public Object getLabel() {
        return day + " " + startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public LocalDate getDate() {
        switch (day) {
            case "Bugun":
                return LocalDate.now();
            case "Ertaga":
                return LocalDate.now().plusDays(1);
            default:
                throw new IllegalArgumentException("Unknown day: " + day);
        }
    }
}
