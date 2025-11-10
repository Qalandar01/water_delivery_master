package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;

@Service
public class OrderBtnCommand implements BotCommand {

    private final String command;
    private final BotServiceIn botServiceIn;

    public OrderBtnCommand(BotServiceIn botServiceIn) {
        this.command = BotConstant.ORDER_BTN;
        this.botServiceIn = botServiceIn;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botServiceIn.startOrdering(message, telegramUser);
    }
}
