package uz.pdp.water_delivery.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.model.dto.OperatorDTO;
import uz.pdp.water_delivery.model.dto.request.OperatorRequestDTO;
import uz.pdp.water_delivery.model.dto.request.UpdateOperatorRequestDTO;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminOperatorController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final LogErrorFile logErrorFile;

    @GetMapping("/admin/operator")
    public String operators(Model model) {
        List<OperatorDTO> operators = userService.getOperatorsDto();
        model.addAttribute("operators", operators);
        return "/admin/operator/operators";
    }

    @GetMapping("/admin/operators/new")
    public String showAddOperatorForm(Model model) {
        model.addAttribute("user", new OperatorRequestDTO());
        return "admin/operator/add-operator";
    }
    @PostMapping("/admin/operators")
    public String createOperatorUser(
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
            return "redirect:/admin/operators/new";
        }
    }


    @GetMapping("/admin/operators/edit/{id}")
    public String showEditOperatorForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/operator/operator-edit";
    }

    @PutMapping("/admin/operators/update/{id}")
    public String updateOperator(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") UpdateOperatorRequestDTO updateOperatorRequestDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "admin/operator/operator-edit";
        }

        try {
            userService.updateUser(id, updateOperatorRequestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logErrorFile.logError(e, "updateUser", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred while updating the user.");
        }

        return "redirect:/admin/operator";
    }
}
