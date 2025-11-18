package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.enums.TelegramState;

@Service
public class CabinetCommand implements BotCommand {
    private final String command;
    private final BotService botService;
    private final TelegramBot telegramBot;

    public CabinetCommand(BotService botService, TelegramBot telegramBot) {
        this.command = BotConstant.CABINET;
        this.botService = botService;
        this.telegramBot = telegramBot;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SETTING)) {
            botService.sendCabinet(message, telegramUser);
        } else {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
