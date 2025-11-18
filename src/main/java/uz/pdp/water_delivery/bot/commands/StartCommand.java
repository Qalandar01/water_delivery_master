package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;

@Service
public class StartCommand implements BotCommand {
    private final BotService botService;
    private final String command;

    public StartCommand(BotService botService) {
        this.botService = botService;
        this.command = BotConstant.START;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botService.acceptStartSendShareContact(message, telegramUser);
    }
}
