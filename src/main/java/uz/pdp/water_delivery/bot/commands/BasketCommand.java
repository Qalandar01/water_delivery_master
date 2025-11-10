package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;

@Service
public class BasketCommand implements BotCommand {

    private final String command;
    private final BotServiceIn botServiceIn;

    public BasketCommand(BotServiceIn botServiceIn) {
        this.command = BotConstant.BASKET;
        this.botServiceIn = botServiceIn;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botServiceIn.showBasket(message, telegramUser);

    }
}
