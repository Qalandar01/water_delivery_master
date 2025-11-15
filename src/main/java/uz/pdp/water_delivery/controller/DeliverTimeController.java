package uz.pdp.water_delivery.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.entity.DeliveryTime;
import uz.pdp.water_delivery.services.service.DeliveryTimeService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DeliverTimeController {

    private final DeliveryTimeService deliveryTimeService;
    private final LogErrorFile logErrorFile;


    @GetMapping("/admin/delivery/time")
    public String deliveryTime(Model model) {
        List<DeliveryTime> deliveryTimes = deliveryTimeService.getAllDeliveryTimes();
        model.addAttribute("deliveryTimes", deliveryTimes);
        return "admin/edit-delivery-time";
    }


    @PostMapping("/admin/update/delivery-time/{id}")
    @ResponseBody
    public ResponseEntity<String> updateDeliveryTime(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        try {
            deliveryTimeService.updateDeliveryTime(id, startTime, endTime);
            return ResponseEntity.ok("Yetkazib berish vaqti muvaffaqiyatli o'zgartirildi!");
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest()
                    .body("Noto'g'ri vaqt formati! Kutilgan format: HH:mm");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logErrorFile.logError(ex, "updateDeliveryTime", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Yetkazib berish vaqtini saqlashda xatolik yuz berdi!");
        }
    }


}
