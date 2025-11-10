package uz.pdp.water_delivery.bot.handlers.state;

import com.pengrad.telegrambot.model.Message;
import uz.pdp.water_delivery.bot.TelegramUser;

public interface StateHandler {
    void handle(Message message, TelegramUser telegramUser);
}
