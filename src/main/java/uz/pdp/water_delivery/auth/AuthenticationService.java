package uz.pdp.water_delivery.auth;

public interface AuthenticationService {
    boolean login(String username, String password);
}
