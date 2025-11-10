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
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String phone,
                        @RequestParam("password") String password,
                        Model model) {
        Optional<User> user = userRepository.findByPhone(phone);

        if (user.isEmpty()) {
            model.addAttribute("error", "Foydalanuvchi topilmadi.");
            return "login";
        }

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            model.addAttribute("error", "Parol noto'g'ri.");
            return "login";
        }

        model.addAttribute("success", "Muvaffaqiyatli login qilindi!");
        return "redirect:/";
    }



    @GetMapping("/change-password")
    public String phoneVerificationPage() {
        return "change-password-phone";
    }

    /**
     * Telefon raqamini tekshiradi va parolni o'zgartirish sahifasiga yo'naltiradi.
     */
    @PostMapping("/change-password")
    public String verifyPhone(@RequestParam("phone") String phone, Model model) {
        Optional<User> user = userRepository.findByPhone(phone);

        if (user.isEmpty()) {
            model.addAttribute("error", "Kiritilgan telefon raqami topilmadi.");
            return "change-password-phone";
        }

        model.addAttribute("phone", phone);
        return "forgot-password";
    }

    /**
     * Parolni o'zgartirishni amalga oshiradi.
     */
    @PostMapping("/forgot-password")
    public String changePassword(@RequestParam("phone") String phone,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {
        Optional<User> user = userRepository.findByPhone(phone);

        if (user.isEmpty()) {
            model.addAttribute("error", "Foydalanuvchi topilmadi.");
            return "change-password-phone";
        }

        User foundUser = user.get();

        if (!passwordEncoder.matches(oldPassword, foundUser.getPassword())) {
            model.addAttribute("error", "Eski parol noto‘g‘ri.");
            model.addAttribute("phone", phone);
            return "forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Yangi parollar mos emas.");
            model.addAttribute("phone", phone);
            return "forgot-password";
        }

        foundUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(foundUser);

        model.addAttribute("success", "Parol muvaffaqiyatli o'zgartirildi.");
        return "redirect:/login";
    }


    @GetMapping("/")
    public String goToUsingRole(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            model.addAttribute("error", "Please login first");
            return "redirect:/login";
        }

        User user = (User) auth.getPrincipal();

        if (Boolean.FALSE.equals(user.getActive())) {
            model.addAttribute("error", "Sizning hisobingiz bloklangan");
            return "login";
        }

        RoleName sessionRoleName = (RoleName) session.getAttribute("selectedRole");
        if (sessionRoleName != null) {
            return redirectToRolePage(sessionRoleName);
        }

        List<Role> roles = user.getAuthorities().stream()
                .filter(grantedAuthority -> grantedAuthority instanceof Role)
                .map(grantedAuthority -> (Role) grantedAuthority)
                .collect(Collectors.toList());

        if (roles.size() == 1) {
            RoleName roleName = roles.get(0).getRoleName();
            session.setAttribute("selectedRole", roleName);
            return redirectToRolePage(roleName);
        }

        if (roles.size() > 1) {
            model.addAttribute("roles", roles);
            return "role-selection";
        }

        model.addAttribute("error", "Access denied or inactive user");
        return "/login";
    }

    @GetMapping("/switch-role")
    public String switchRole(@RequestParam RoleName roleName, HttpSession session) {
        session.setAttribute("selectedRole", roleName);
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
