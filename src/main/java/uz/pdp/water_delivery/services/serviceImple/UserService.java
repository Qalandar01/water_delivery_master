package uz.pdp.water_delivery.services.serviceImple;

import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.entity.User;
import uz.pdp.water_delivery.entity.enums.RoleName;

import java.util.List;

public interface UserService  {
    User createdOrFindUser(String contact);

    User createOrUpdateUser(UserDTO userDTO);

    User findByPhone(String phone);

    List<User> getUsersByRole(RoleName roleName);

}
