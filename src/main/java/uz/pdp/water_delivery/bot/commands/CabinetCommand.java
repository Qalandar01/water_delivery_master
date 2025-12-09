package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.BotNavigationService;
import uz.pdp.water_delivery.model.enums.TelegramState;

@Service
public class CabinetCommand implements BotCommand {
    private final String command;
    private final TelegramBot telegramBot;
    private final BotNavigationService botNavigationService;

    public CabinetCommand( TelegramBot telegramBot, BotNavigationService botNavigationService) {
        this.command = BotConstant.CABINET;
        this.telegramBot = telegramBot;
        this.botNavigationService = botNavigationService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SETTING)) {
            botNavigationService.sendCabinet(message, telegramUser);
        } else {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
