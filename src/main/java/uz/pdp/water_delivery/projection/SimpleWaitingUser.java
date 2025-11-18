package uz.pdp.water_delivery.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SimpleWaitingUser {

    Long getId();
    String getUserPhone();
    LocalDateTime getCreatedAt();

}
