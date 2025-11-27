package uz.pdp.water_delivery.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.dto.ProductDTO;
import uz.pdp.water_delivery.dto.ProductEditView;
import uz.pdp.water_delivery.services.ProductService;
import uz.pdp.water_delivery.utils.LogErrorFile;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final LogErrorFile logErrorFile;

    @GetMapping("/admin/add/product")
    public String showAddProductForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        return "admin/product/add-product";
    }

    @PostMapping("/admin/add/product")
    public String addProduct(
            @ModelAttribute ProductDTO productDto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.createProduct(productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Idish turi qo'shildi!");
            return "redirect:/admin/product/menu";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/add/product";

        } catch (Exception e) {
            logErrorFile.logError(e, "AddProduct", null);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ma'lumotni saqlashda yoki faylni yuklashda xatolik yuz berdi!");
            return "redirect:/admin/add/product";
        }
    }

    @GetMapping("/admin/product/menu")
    public String productMenu(Model model) {
        model.addAttribute("products", productService.getActiveProductsWithOrderCount());
        return "/admin/product/product-menu";
    }

    @GetMapping("/admin/product/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductEditView view = productService.getProductEditView(id);
        model.addAttribute("product", view.getDto());
        model.addAttribute("base64Image", view.getBase64Image());
        return "/admin/product/product-edit";
    }

    @PutMapping("/admin/product/update")
    public String updateProduct(@ModelAttribute ProductDTO productDto) {
        try {
            productService.updateProduct(productDto);
            return "redirect:/admin/product/menu";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateProduct", productDto.getId());
            return "redirect:/admin/product/menu?error=true";
        }
    }

    @DeleteMapping("/admin/product/delete/{id}")
    public String deleteBottle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete product as it is associated with existing orders.");
            logErrorFile.logError(e, "deleteProduct", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred.");
            logErrorFile.logError(e, "deleteProduct", id);
        }

        return "redirect:/admin/product/menu";
    }
}
