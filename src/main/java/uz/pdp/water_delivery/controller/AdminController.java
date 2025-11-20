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
import uz.pdp.water_delivery.services.service.ProductService;
import uz.pdp.water_delivery.services.service.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {


    private final LogErrorFile logErrorFile;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/admin")
    public String admin(Model model) {
        List<User> operators = userService.getUsersByRole(RoleName.ROLE_OPERATOR);
        model.addAttribute("users", operators);
        return "admin/admin";
    }


    @GetMapping("/admin/change-gift-water")
    public String changeGiftWater(Model model) {
        model.addAttribute("bottleTypes", productService.getActiveProductsWithOrderCount());
        return "admin/chegirmalar";
    }

    @PostMapping("/admin/change-gift-water")
    public String changeGiftWater(GiftWaterRequest request, RedirectAttributes redirectAttributes) {

        try {
            productService.updateGiftWater(request);
            redirectAttributes.addFlashAttribute("successMessage", "Chegirma muvaffaqiyatli saqlandi.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
        }

        return "redirect:/admin/change-gift-water";
    }


    /**
     * Chegirma o'chirish
     */

    @DeleteMapping("/admin/discount/{id}")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            productService.deleteDiscount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Chegirma muvaffaqiyatli o'chirildi.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xatolik yuz berdi: " + e.getMessage());
        }

        return "redirect:/admin/change-gift-water";
    }


    @GetMapping("/admin/add/bottle")
    public String showAddProductForm(Model model) {
        model.addAttribute("bottleTypeDTO", new ProductDTO());
        return "admin/bottle/add-bottle-type";
    }

    @PostMapping("/admin/add/bottle")
    public String addProduct(
            @ModelAttribute ProductDTO productDto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.createProduct(productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Idish turi qo'shildi!");
            return "redirect:/admin/bottle/menu";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/add/bottle";

        } catch (Exception e) {
            logErrorFile.logError(e, "addBottle", null);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ma'lumotni saqlashda yoki faylni yuklashda xatolik yuz berdi!");
            return "redirect:/admin/add/bottle";
        }
    }

    @GetMapping("/admin/bottle/menu")
    public String productMenu(Model model) {
        model.addAttribute("bottleTypes", productService.getActiveProductsWithOrderCount());
        return "admin/bottle/bottle-menu";
    }

    @GetMapping("/admin/bottle/edit/{id}")
    public String editBottle(@PathVariable Long id, Model model) {
        ProductEditView view = productService.getProductEditView(id);
        model.addAttribute("bottleType", view.getDto());
        model.addAttribute("base64Image", view.getBase64Image());
        return "admin/bottle/bottle-edit";
    }

    @PostMapping("/admin/bottle/update")
    public String updateBottle(@ModelAttribute ProductDTO productDto) {
        try {
            productService.updateProduct(productDto);
            return "redirect:/admin/bottle/menu";
        } catch (Exception e) {
            logErrorFile.logError(e, "updateBottle", productDto.getId());
            return "redirect:/admin/bottle/menu?error=true";
        }
    }

    @DeleteMapping("/admin/bottle/delete/{id}")
    public String deleteBottle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bottle type deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete bottle type as it is associated with existing orders.");
            logErrorFile.logError(e, "deleteBottle", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred.");
            logErrorFile.logError(e, "deleteBottle", id);
        }

        return "redirect:/admin/bottle/menu";
    }


    @GetMapping("/admin/users/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/add-user";
    }

    @PostMapping("/admin/users")
    public String createUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (userRepository.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "This phone number is already in use.");
        }

        if (result.hasErrors()) {
            return "admin/add-user";
        }

        try {
            userService.createOperatorUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while creating user.");
            return "redirect:/admin/users/new";
        }
    }


    @DeleteMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteOrUpdateUserRoles(id);
            redirectAttributes.addFlashAttribute("successMessage", "User updated or deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logErrorFile.logError(e, "deleteUser", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while updating user roles.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/admin/users/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") UserRequestDTO userRequestDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "admin/edit-user";
        }

        try {
            userService.updateUser(id, userRequestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logErrorFile.logError(e, "updateUser", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred while updating the user.");
        }

        return "redirect:/admin";
    }


}
