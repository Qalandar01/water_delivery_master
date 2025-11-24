package uz.pdp.water_delivery.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.entity.DeliveryTime;
import uz.pdp.water_delivery.repo.DeliveryTimeRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryTimeService {
    private final DeliveryTimeRepository deliveryTimeRepository;

    public List<DeliveryTime> getAllDeliveryTimes() {
        return deliveryTimeRepository.findAllByOrderByIdAsc();
    }
    @Transactional
    public void updateDeliveryTime(Long id, String startTimeStr, String endTimeStr) {

        // Parse times
        LocalTime start = LocalTime.parse(startTimeStr);
        LocalTime end = LocalTime.parse(endTimeStr);

        // Validate time order
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Boshlanish vaqti tugash vaqtidan keyin bo'lishi mumkin emas!");
        }

        // Optional: enforce time-of-day update window (commented in original)
        /*
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(23, 0)) || now.isAfter(LocalTime.of(23, 50))) {
            throw new IllegalStateException(
                "Yetkazib berish vaqtini faqat 23:00 va 23:50 oralig'ida o'zgartirishingiz mumkin."
            );
        }
        */

        DeliveryTime deliveryTime = deliveryTimeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Yetkazib berish vaqti topilmadi!"));

        // Update and save
        deliveryTime.setStartTime(start);
        deliveryTime.setEndTime(end);
        deliveryTimeRepository.save(deliveryTime);
    }
}
