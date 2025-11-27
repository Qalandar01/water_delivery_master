package uz.pdp.water_delivery.controller.superAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.services.UserService;

@Controller
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserRepository userRepository;
    private final UserService userService;


    @GetMapping("/super-admin")
    public String superAdmin(Model model, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        model.addAttribute("users", userService.findAdminsExcluding(currentUser.getId()));

        return "superAdmin/super-admin";
    }



    @GetMapping("/super-admin/add/user")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "superAdmin/add-user";
    }

    @PostMapping("/super-admin/add/user")
    public String addUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result
    ) {
        userService.createAdmin(user, result);

        if (result.hasErrors()) {
            return "superAdmin/add-user";
        }

        return "redirect:/super-admin";
    }


    @DeleteMapping("/super-admin/delete/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return "redirect:/super-admin";
    }


    @GetMapping("/super-admin/edit/user/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        return "superAdmin/edit-user";
    }

    @PutMapping("/super-admin/edit/user/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") User user,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "superAdmin/edit-user";
        }

        userService.updateUser(id, user);
        return "redirect:/super-admin";
    }

}
