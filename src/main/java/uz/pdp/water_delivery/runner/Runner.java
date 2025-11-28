package uz.pdp.water_delivery.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.pdp.water_delivery.model.entity.*;
import uz.pdp.water_delivery.model.enums.RoleName;
import uz.pdp.water_delivery.repo.*;

import java.util.List;

//@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CourierRepository courierRepository;

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAllByRolesRoleName(RoleName.ROLE_ADMIN);
        if (users.isEmpty()) {
            User userSuperAdmin = new User();
            userSuperAdmin.setFirstName("Abdusobur");
            userSuperAdmin.setLastName("Halimov");
            userSuperAdmin.setPhone("+998941211112");
            userSuperAdmin.setPassword(passwordEncoder.encode("12345"));
            userSuperAdmin.setRoles(roleRepository.findAll());
            userRepository.save(userSuperAdmin);
        }

    }

}
