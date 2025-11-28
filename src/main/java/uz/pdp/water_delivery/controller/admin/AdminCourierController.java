package uz.pdp.water_delivery.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.model.dto.request.courier.CourierRequestDTO;
import uz.pdp.water_delivery.model.entity.Courier;
import uz.pdp.water_delivery.services.CourierService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminCourierController {

    private final CourierService courierService;
    private final LogErrorFile logErrorFile;

    @GetMapping("/admin/couriers")
    public String couriers(Model model) {
        model.addAttribute("couriers", courierService.getAllCouriersWithOrderStatus());
        return "admin/courier/couriers";
    }


    @GetMapping("/admin/couriers/new")
    public String showCreateCourierForm(Model model) {
        model.addAttribute("courierDTO", new CourierRequestDTO());
        return "admin/courier/courier-form";
    }

    @Transactional
    @PostMapping("/admin/couriers/save")
    public String saveCourier(
            @Valid @ModelAttribute("courierDTO") CourierRequestDTO courierRequestDTO,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Formda xatoliklar mavjud. Iltimos, tekshirib qayta kiriting.");
            return "admin/courier/courier-form";
        }

        try {
            courierService.saveCourier(courierRequestDTO);
            return "redirect:/admin/couriers";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/courier/courier-form";

        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Kuryerni saqlashda xatolik yuz berdi.");
            return "admin/courier/courier-form";
        }
    }

    @GetMapping("/admin/couriers/edit/{id}")
    public String editCourierForm(@PathVariable Long id, Model model) {

        Courier courier = courierService.findByIdOrThrow(id);
        List<String > districts = List.of("Chilonzor","Yunusobod","Shayxontoxur","Mirzo Ulugbek");

        model.addAttribute("courier", courier);
        model.addAttribute("districts", districts);

        return "admin/courier/courier-edit";
    }


    @Transactional
    @PutMapping("/admin/couriers/update/{id}")
    public String updateCourier(
            @PathVariable Long id,
            @ModelAttribute CourierRequestDTO courier,
            Model model
    ) {
        try {
            courierService.updateCourier(id, courier);
            return "redirect:/admin/couriers";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/courier/courier-edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Kuryerni yangilashda xatolik yuz berdi.");
            logErrorFile.logError(e, "updateCourier", id);
            return "admin/courier/courier-edit";
        }
    }

    @Transactional
    @DeleteMapping("/admin/couriers/delete/{id}")
    public String deleteCourier(@PathVariable Long id, Model model) {
        try {
            courierService.deleteCourierById(id);
            model.addAttribute("successMessage", "Kuryer muvaffaqiyatli o'chirildi.");
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (Exception e) {
            logErrorFile.logError(e, "deleteCourier", id);
            model.addAttribute("errorMessage", "Kuryerni o'chirishda xatolik yuz berdi.");
        }
        return "redirect:/admin/couriers";
    }
}
