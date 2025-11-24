package uz.pdp.water_delivery.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.dto.ProductDTO;
import uz.pdp.water_delivery.dto.ProductEditView;
import uz.pdp.water_delivery.dto.request.GiftWaterRequest;
import uz.pdp.water_delivery.dto.request.UserRequestDTO;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.repo.*;
import uz.pdp.water_delivery.services.ProductService;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {


    private final LogErrorFile logErrorFile;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProductService productService;



//
//    @GetMapping("/admin/change-gift-water")
//    public String changeGiftWater(Model model) {
//        model.addAttribute("bottleTypes", productService.getActiveProductsWithOrderCount());
//        return "admin/chegirmalar";
//    }
//
//    @PostMapping("/admin/change-gift-water")
//    public String changeGiftWater(GiftWaterRequest request, RedirectAttributes redirectAttributes) {
//
//        try {
//            productService.updateGiftWater(request);
//            redirectAttributes.addFlashAttribute("successMessage", "Chegirma muvaffaqiyatli saqlandi.");
//        } catch (IllegalArgumentException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
//        }
//
//        return "redirect:/admin/change-gift-water";
//    }


    /**
     * Chegirma o'chirish
     */

//    @DeleteMapping("/admin/discount/{id}")
//    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
//
//        try {
//            productService.deleteDiscount(id);
//            redirectAttributes.addFlashAttribute("successMessage", "Chegirma muvaffaqiyatli o'chirildi.");
//        } catch (IllegalArgumentException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
//        }
//
//        return "redirect:/admin/change-gift-water";
//    }







}
