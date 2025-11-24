package uz.pdp.water_delivery.controller.superAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.repo.RoleRepository;
import uz.pdp.water_delivery.repo.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/super-admin")
    public String superAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        List<User> users = userRepository.findAllByRolesRoleName(RoleName.ROLE_ADMIN)
                .stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        model.addAttribute("users", users);
        return "superAdmin/super-admin";
    }


    @GetMapping("/super-admin/add/user")
    public String addUser(Model model) {

        User user = new User();
        model.addAttribute("user", user);
        return "superAdmin/add-user";
    }

    @PostMapping("/super-admin/add/user")
    public String addUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (userRepository.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "This phone number is already in use.");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "superAdmin/add-user";
        }
        Role roleOperator = roleRepository.findByRoleName(RoleName.ROLE_ADMIN);
        user.setRoles(List.of(roleOperator));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/super-admin";
    }

    @DeleteMapping("/super-admin/delete/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        user.setIsDeleted(true);
        userRepository.save(user);
        return "redirect:/super-admin";
    }

    @GetMapping("/super-admin/edit/user/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        return "superAdmin/edit-user";
    }

    @PostMapping("/super-admin/edit/user/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return "superAdmin/edit-user";
        }
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setActive(user.getActive());
        existingUser.setPaid(user.getPaid());

        if (user.getPaid()) { // Agar foydalanuvchi to'lov qilgan bo'lsa
            existingUser.setPaidDate(LocalDate.now()); // To'lov sanasini yangilash
            existingUser.setNextMonthDate(LocalDate.now().plusMonths(1)); // Keyingi to'lov sanasi
        }

        userRepository.save(existingUser);
        return "redirect:/super-admin";
    }







}
