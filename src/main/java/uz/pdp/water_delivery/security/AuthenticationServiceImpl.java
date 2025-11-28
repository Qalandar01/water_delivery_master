package uz.pdp.water_delivery.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.repo.UserRepository;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean login(String username, String password) {

        Optional<User> user = userRepository.findByPhone(username);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
    }
}
