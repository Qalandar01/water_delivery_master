package uz.pdp.water_delivery.controller.admin;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.dto.request.UserRequestDTO;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final LogErrorFile logErrorFile;

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
            return "redirect:/admin";
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
