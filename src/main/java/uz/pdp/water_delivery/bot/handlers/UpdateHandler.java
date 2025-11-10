package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.Update;
import org.springframework.transaction.annotation.Transactional;

public interface UpdateHandler {
    boolean canHandle(Update update);

    void handle(Update update);
}
