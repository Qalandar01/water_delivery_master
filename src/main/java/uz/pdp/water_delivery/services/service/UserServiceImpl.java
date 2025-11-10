package uz.pdp.water_delivery.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.exception.DuplicatePhoneNumberException;
import uz.pdp.water_delivery.repo.RoleRepository;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.services.serviceImple.UserService;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createdOrFindUser(String contact) {
        Optional<User> users = userRepository.findByPhone(contact);
        User user = new User();
        user.setPhone(contact);
        return users.orElseGet(() -> userRepository.save(user));
    }

    @Override
    public User createOrUpdateUser(UserDTO userDTO) throws DuplicatePhoneNumberException {
        String repairedPhone = PhoneRepairUtil.repair(userDTO.getPhone());
        Optional<User> existingUser = userRepository.findByPhone(repairedPhone);
        User user = existingUser.orElse(User.builder().build());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(repairedPhone);
        Role role = roleRepository.findByRoleName(RoleName.ROLE_DELIVERY);
        if (role != null) {
            user.setRoles(List.of(role));
        }
        userRepository.save(user);
        return user;
    }

    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(PhoneRepairUtil.repair(phone)).orElse(null);
    }

}
