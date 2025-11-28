package uz.pdp.water_delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import uz.pdp.water_delivery.model.repo.UserRepository;
import uz.pdp.water_delivery.services.ProductService;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

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
