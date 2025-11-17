package uz.pdp.water_delivery.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.pdp.water_delivery.auth.AuthenticationService;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.repo.TelegramUserRepository;
import uz.pdp.water_delivery.repo.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationService authenticationService;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }


    @GetMapping("/change-password")
    public String showChangePasswordPhonePage() {
        return "change-password-phone";
    }

    /**
     * Telefon raqamini tekshiradi va parolni o'zgartirish sahifasiga yo'naltiradi.
     */

    @PostMapping("/change-password")
    public String verifyPhone(
            @RequestParam String phone,
            Model model
    ) {
        userRepository.findByPhone(phone)
                .ifPresentOrElse(
                        user -> model.addAttribute("phone", phone),
                        () -> model.addAttribute("error", "Kiritilgan telefon raqami topilmadi.")
                );

        return model.containsAttribute("error")
                ? "change-password-phone"
                : "forgot-password";
    }


    /**
     * Parolni o'zgartirishni amalga oshiradi.
     */
    @PostMapping("/forgot-password")
    public String changePassword(
            @RequestParam String phone,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model
    ) {
        Optional<User> optionalUser = userRepository.findByPhone(phone);

        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Foydalanuvchi topilmadi.");
            return "change-password-phone";
        }

        User user = optionalUser.get();

        // Check old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Eski parol noto‘g‘ri.");
            model.addAttribute("phone", phone);
            return "forgot-password";
        }

        // Check password confirmation
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Yangi parollar mos emas.");
            model.addAttribute("phone", phone);
            return "forgot-password";
        }

        // Optional: prevent reusing the same password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("error", "Yangi parol eski paroldan farq qilishi kerak.");
            model.addAttribute("phone", phone);
            return "forgot-password";
        }

        // Save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/login?success=password-changed";
    }

    @GetMapping("/")
    public String goToUsingRole(Model model, HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // --- 1. AUTHENTICATION CHECK ---
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return "redirect:/login?error=not-logged-in";
        }

        User user = (User) auth.getPrincipal();

        // --- 2. CHECK IF USER IS ACTIVE ---
        if (Boolean.FALSE.equals(user.getActive())) {
            model.addAttribute("error", "Sizning hisobingiz bloklangan");
            return "login";
        }

        // --- 3. CHECK IF ROLE WAS ALREADY SELECTED IN SESSION ---
        RoleName storedRole = (RoleName) session.getAttribute("selectedRole");
        if (storedRole != null) {
            return redirectToRolePage(storedRole);
        }

        // --- 4. USER ROLES ---
        List<Role> roles = user.getAuthorities().stream()
                .filter(Role.class::isInstance)
                .map(Role.class::cast)
                .toList();

        if (roles.isEmpty()) {
            return "redirect:/login?error=no-roles";
        }

        // --- 5. IF USER HAS ONLY ONE ROLE, AUTO-SELECT IT ---
        if (roles.size() == 1) {
            RoleName roleName = roles.get(0).getRoleName();
            session.setAttribute("selectedRole", roleName);
            return redirectToRolePage(roleName);
        }

        // --- 6. USER HAS MULTIPLE ROLES → SHOW ROLE SELECTION PAGE ---
        model.addAttribute("roles", roles);
        return "role-selection";
    }


    @GetMapping("/switch-role")
    public String switchRole(@RequestParam("roleName") RoleName roleName, HttpSession session) {
        // --- 1. Validate the roleName ---
        if (roleName == null) {
            return "redirect:/?error=invalid-role";
        }

        // --- 2. Store selected role in session ---
        session.setAttribute("selectedRole", roleName);

        // --- 3. Redirect to appropriate role page ---
        return redirectToRolePage(roleName);
    }



    private String redirectToRolePage(RoleName roleName) {
        String page = switch (roleName) {
            case ROLE_SUPER_ADMIN -> "/super-admin";
            case ROLE_ADMIN -> "/admin";
            case ROLE_OPERATOR -> "/operator";
            default -> "/logout";
        };
        return "redirect:" + page;
    }


}
