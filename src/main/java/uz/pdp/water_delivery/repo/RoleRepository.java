package uz.pdp.water_delivery.repo;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.water_delivery.entity.Role;
import uz.pdp.water_delivery.entity.enums.RoleName;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(RoleName roleName);

    List<Role> findByRoleNameIn(List<RoleName> roleAdmin);

    List<Role> findAllByRoleNameIn(List<RoleName> roleAdmin);
}