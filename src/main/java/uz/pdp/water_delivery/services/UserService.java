package uz.pdp.water_delivery.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uz.pdp.water_delivery.model.records.operator.OperatorResponseDTO;
import uz.pdp.water_delivery.model.dto.request.courier.CourierRequestDTO;
import uz.pdp.water_delivery.model.dto.request.operator.OperatorRequestDTO;
import uz.pdp.water_delivery.model.dto.request.operator.UpdateOperatorRequestDTO;
import uz.pdp.water_delivery.model.entity.Role;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.enums.RoleName;
import uz.pdp.water_delivery.exception.DuplicatePhoneNumberException;
import uz.pdp.water_delivery.model.repo.RoleRepository;
import uz.pdp.water_delivery.model.repo.UserRepository;
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

    public User createOrUpdateUser(CourierRequestDTO courierRequestDTO) throws DuplicatePhoneNumberException {
        String repairedPhone = PhoneRepairUtil.repair(courierRequestDTO.getPhone());
        Optional<User> existingUser = userRepository.findByPhone(repairedPhone);
        User user = existingUser.orElse(User.builder().build());
        user.setFirstName(courierRequestDTO.getFirstName());
        user.setLastName(courierRequestDTO.getLastName());
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
    public void createOperatorUser(OperatorRequestDTO operatorRequest) {
        Role roleOperator = roleRepository.findByRoleName(RoleName.ROLE_OPERATOR);
        User user = User.builder()
                .firstName(operatorRequest.getFirstname())
                .lastName(operatorRequest.getLastname())
                .password(passwordEncoder.encode(operatorRequest.getPassword()))
                .roles(List.of(roleOperator))
                .active(true)
                .isDeleted(false)
                .Paid(false)
                .phone(operatorRequest.getPhone())
                .build();
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
    public UpdateOperatorRequestDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return UpdateOperatorRequestDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .active(user.getActive())
                .paid(user.getPaid())
                .build();
    }

    @Transactional
    public void updateUser(Long id, @Valid UpdateOperatorRequestDTO updateOperatorRequestDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        existingUser.setFirstName(updateOperatorRequestDTO.getFirstName());
        existingUser.setLastName(updateOperatorRequestDTO.getLastName());
        existingUser.setPhone(updateOperatorRequestDTO.getPhone());
        existingUser.setActive(updateOperatorRequestDTO.isActive());
        existingUser.setPaid(updateOperatorRequestDTO.isPaid());

        if (updateOperatorRequestDTO.isPaid()) {
            existingUser.setPaidDate(LocalDate.now());
            existingUser.setNextMonthDate(LocalDate.now().plusMonths(1));
        }

        userRepository.save(existingUser);

    }

    public List<User> findAdminsExcluding(Long userId) {
        return userRepository.findAllByRolesRoleName(RoleName.ROLE_ADMIN)
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .toList();
    }

    public void createAdmin(User user, BindingResult result) {

        if (userRepository.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "duplicate", "This phone number is already in use.");
            return;
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN);

        user.setRoles(List.of(adminRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setActive(updatedUser.getActive());

        boolean paidBefore = existingUser.getPaid();
        existingUser.setPaid(updatedUser.getPaid());

        // Payment logic improvement
        if (!paidBefore && updatedUser.getPaid()) {
            existingUser.setPaidDate(LocalDate.now());
            existingUser.setNextMonthDate(LocalDate.now().plusMonths(1));
        }
    }

    public List<OperatorResponseDTO> getOperatorsDto() {
        List<User> users = getUsersByRole(RoleName.ROLE_OPERATOR);

        return users.stream()
                .map(user -> new OperatorResponseDTO(
                        user.getId(),
                        user.getLastName(),
                        user.getDisplayPhone(),
                        user.getActive()
                )).toList();

    }

}
