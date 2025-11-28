package uz.pdp.water_delivery.controller.admin;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.model.dto.request.OperatorRequestDTO;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final LogErrorFile logErrorFile;

    @PostMapping("/admin/users")
    public String createUser(
            @Valid @ModelAttribute("user") OperatorRequestDTO user,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (userRepository.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "This phone number is already in use.");
        }

        if (result.hasErrors()) {
            return "admin/operator/add-operator";
        }

        try {
            userService.createOperatorUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/operator";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while creating user.");
            return "redirect:/admin/users/new";
        }
    }


    @DeleteMapping("/admin/delete/user/{id}")
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
        return "redirect:/admin/operator";
    }


}
