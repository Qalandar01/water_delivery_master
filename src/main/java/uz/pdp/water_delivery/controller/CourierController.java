package uz.pdp.water_delivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.entity.Courier;
import uz.pdp.water_delivery.entity.District;
import uz.pdp.water_delivery.services.service.CourierService;
import uz.pdp.water_delivery.services.service.DistrictService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourierController {

    private final LogErrorFile logErrorFile;
    private final CourierService courierService;
    private final DistrictService districtService;


    @GetMapping("/admin/couriers")
    public String couriers(Model model) {
        model.addAttribute("couriers", courierService.getAllCouriersWithOrderStatus());
        return "admin/couriers";
    }


    @GetMapping("/admin/couriers/new")
    public String showCreateCourierForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "admin/courier-form";
    }

    @Transactional
    @PostMapping("/admin/couriers/save")
    public String saveCourier(
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Formda xatoliklar mavjud. Iltimos, tekshirib qayta kiriting.");
            return "admin/courier-form";
        }

        try {
            courierService.saveCourier(userDTO);
            return "redirect:/admin/couriers";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/courier-form";

        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Kuryerni saqlashda xatolik yuz berdi.");
            return "admin/courier-form";
        }
    }

    @GetMapping("/admin/couriers/edit/{id}")
    public String editCourierForm(@PathVariable Long id, Model model) {

        Courier courier = courierService.findByIdOrThrow(id);
        List<District> districts = districtService.getAllDistricts();

        model.addAttribute("courier", courier);
        model.addAttribute("districts", districts);

        return "admin/courier-edit";
    }


    @Transactional
    @PostMapping("/admin/couriers/update/{id}")
    public String updateCourier(
            @PathVariable Long id,
            @ModelAttribute Courier courier,
            Model model
    ) {
        try {
            courierService.updateCourier(id, courier);
            return "redirect:/admin/couriers";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/courier-edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Kuryerni yangilashda xatolik yuz berdi.");
            logErrorFile.logError(e, "updateCourier", id);
            return "admin/courier-edit";
        }
    }

    @Transactional
    @GetMapping("/admin/couriers/delete/{id}")
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

