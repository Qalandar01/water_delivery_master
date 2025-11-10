package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import uz.pdp.water_delivery.bot.TelegramUser;

public interface BotCommand {
    void execute(Message message, TelegramUser telegramUser);
    String getCommand();
}
