package uz.pdp.water_delivery.exception;

public class TelegramUserNotFoundException extends RuntimeException {

    public TelegramUserNotFoundException(Long userId) {
        super("Telegram user not found: " + userId);
    }}
