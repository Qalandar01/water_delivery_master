package uz.pdp.water_delivery.security;

public interface AuthenticationService {
    boolean login(String username, String password);
}
