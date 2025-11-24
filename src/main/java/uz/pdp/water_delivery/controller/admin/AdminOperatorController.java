package uz.pdp.water_delivery.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.services.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminOperatorController {

    private final UserService userService;

    @GetMapping("/admin")
    public String operators(Model model) {
        List<User> operators = userService.getUsersByRole(RoleName.ROLE_OPERATOR);
        model.addAttribute("operators", operators);
        return "/admin/operators";
    }
}
