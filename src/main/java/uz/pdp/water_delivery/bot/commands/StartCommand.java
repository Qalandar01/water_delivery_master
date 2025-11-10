package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;

@Service
public class StartCommand implements BotCommand {
    private final BotServiceIn botServiceIn;
    private final String command;

    public StartCommand(BotServiceIn botServiceIn) {
        this.botServiceIn = botServiceIn;
        this.command = BotConstant.START;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botServiceIn.acceptStartSendShareContact(message, telegramUser);
    }
}
