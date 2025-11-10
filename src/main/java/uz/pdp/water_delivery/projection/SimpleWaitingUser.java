package uz.pdp.water_delivery.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SimpleWaitingUser {

    UUID getId();
    String getUserPhone();
    LocalDateTime getCreatedAt();

}
