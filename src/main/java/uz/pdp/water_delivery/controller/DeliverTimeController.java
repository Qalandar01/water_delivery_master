package uz.pdp.water_delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.entity.DeliveryTime;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.repo.DeliveryTimeRepository;
import uz.pdp.water_delivery.repo.RoleRepository;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DeliverTimeController {

    private final DeliveryTimeRepository deliveryTimeRepository;
    private final RoleRepository roleRepository;


    @GetMapping("/admin/delivery/time")
    public String deliverTime(Model model) {
        List<DeliveryTime> deliveryTimes = deliveryTimeRepository.findAllByOrderByIdAsc();
        model.addAttribute("deliveryTimes", deliveryTimes);
        return "admin/edit-delivery-time";
    }

    @PostMapping("/admin/update/delivery-time/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDeliveryTime(@PathVariable Long id,
                                                @RequestParam String startTime,
                                                @RequestParam String endTime) {
        LocalTime start;
        LocalTime end;

        try {
            start = LocalTime.parse(startTime);
            end = LocalTime.parse(endTime);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Noto'g'ri vaqt formati! Kutilgan format: HH:mm");
        }
/*
        // Faqat 23:00 va 23:50 oralig'ida ruxsat
        if (!LocalTime.now().isAfter(LocalTime.of(23, 0)) || !LocalTime.now().isBefore(LocalTime.of(23, 50))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Yetkazib berish vaqtini faqat 23:00 va 23:50 oralig'ida o'zgartirishingiz mumkin.");
        }*/

        if (start.isAfter(end)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Boshlanish vaqti tugash vaqtidan keyin bo'lishi mumkin emas!");
        }

        Optional<DeliveryTime> optionalDeliveryTime = deliveryTimeRepository.findById(id);
        if (optionalDeliveryTime.isPresent()) {
            DeliveryTime deliveryTime = optionalDeliveryTime.get();
            deliveryTime.setStartTime(start);
            deliveryTime.setEndTime(end);
            try {
                deliveryTimeRepository.save(deliveryTime);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Yetkazib berish vaqtini saqlashda xatolik yuz berdi!");
            }
            return ResponseEntity.ok("Yetkazib berish vaqti muvaffaqiyatli o'zgartirildi!");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Yetkazib berish vaqti topilmadi!");
    }






}
