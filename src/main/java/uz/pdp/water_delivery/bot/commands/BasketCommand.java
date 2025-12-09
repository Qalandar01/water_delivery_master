package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.BasketBotService;

@Service
public class BasketCommand implements BotCommand {

    private final String command;
    private final BasketBotService basketBotService;

    public BasketCommand( BasketBotService basketBotService) {
        this.command = BotConstant.BASKET;
        this.basketBotService = basketBotService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        basketBotService.showBasket(message, telegramUser);

    }
}
