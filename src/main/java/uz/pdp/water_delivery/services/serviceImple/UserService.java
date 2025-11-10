package uz.pdp.water_delivery.services.serviceImple;

import uz.pdp.water_delivery.dto.UserDTO;
import uz.pdp.water_delivery.entity.User;

public interface UserService  {
    User createdOrFindUser(String contact);

    User createOrUpdateUser(UserDTO userDTO);

    User findByPhone(String phone);
}
