package uz.pdp.water_delivery.repo;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    List<User> searchUsersByPhone(String keyword);

    List<User> findAllByRolesRoleName(RoleName roleName);


    boolean existsByPhone(@NotBlank(message = "Phone number is required.") String phone);

    @Query(value = "select * from users u\n" +
            "join user_roles ur on u.id = ur.\"user_id\"\n" +
            "join roles r on r.id = ur.role_id\n" +
            "where r.role_name = 'ROLE_ADMIN' or r.role_name = 'ROLE_OPERATOR';" ,nativeQuery = true)
    List<User> findAllByRolesIn();

    @Query(value = "SELECT DISTINCT u.* " +
            "FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.role_name = 'ROLE_OPERATOR' " +
            "OR (r.role_name = 'ROLE_OPERATOR' AND EXISTS (" +
            "    SELECT 1 " +
            "    FROM user_roles ur2 " +
            "    JOIN roles r2 ON ur2.role_id = r2.id " +
            "    WHERE ur2.user_id = u.id AND r2.role_name = 'ROLE_USER'))",
            nativeQuery = true)
    List<User> findAllByRoleOperatorOrOperatorAndUser();

}