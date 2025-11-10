package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.enums.TelegramState;

@Service
public class MyOrdersCommand implements BotCommand {

    private final String command;
    private final BotServiceIn botServiceIn;
    private final TelegramBot telegramBot;

    public MyOrdersCommand(BotServiceIn botServiceIn, TelegramBot telegramBot) {
        this.command = BotConstant.MY_ORDERS;
        this.botServiceIn = botServiceIn;
        this.telegramBot = telegramBot;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SETTING)) {
            botServiceIn.sendMyOrders(message, telegramUser);
        } else {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
