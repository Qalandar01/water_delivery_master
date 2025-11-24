package uz.pdp.water_delivery.exception;

public class UserDeletionException extends RuntimeException {
    public UserDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}