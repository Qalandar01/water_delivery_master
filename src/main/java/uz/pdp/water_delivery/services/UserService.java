package uz.pdp.water_delivery.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.dto.request.UserRequestDTO;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;
import uz.pdp.water_delivery.exception.DuplicatePhoneNumberException;
import uz.pdp.water_delivery.repo.RoleRepository;
import uz.pdp.water_delivery.repo.UserRepository;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createdOrFindUser(String contact) {
        Optional<User> users = userRepository.findByPhone(contact);
        User user = new User();
        user.setPhone(contact);
        return users.orElseGet(() -> userRepository.save(user));
    }

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

    public User findByPhone(String phone) {
        return userRepository.findByPhone(PhoneRepairUtil.repair(phone)).orElse(null);
    }

    public List<User> getUsersByRole(RoleName roleName) {
        return userRepository.findAllByRolesRoleName(roleName);
    }

    @Transactional
    public void createOperatorUser(User user) {
        Role roleOperator = roleRepository.findByRoleName(RoleName.ROLE_OPERATOR);
        user.setRoles(List.of(roleOperator));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteOrUpdateUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Set<Role> remainingRoles = user.getRoles().stream()
                .filter(role -> !role.getRoleName().equals(RoleName.ROLE_OPERATOR))
                .collect(Collectors.toSet());

        if (remainingRoles.isEmpty()) {
            user.setIsDeleted(true);
            userRepository.save(user);
        } else {
            user.setRoles(List.copyOf(remainingRoles));
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public void updateUser(Long id, @Valid UserRequestDTO userRequestDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        existingUser.setFirstName(userRequestDTO.getFirstName());
        existingUser.setLastName(userRequestDTO.getLastName());
        existingUser.setPhone(userRequestDTO.getPhone());
        existingUser.setActive(userRequestDTO.isActive());
        existingUser.setPaid(userRequestDTO.isPaid());

        if (userRequestDTO.isPaid()) {
            existingUser.setPaidDate(LocalDate.now());
            existingUser.setNextMonthDate(LocalDate.now().plusMonths(1));
        }

        userRepository.save(existingUser);

    }
}
