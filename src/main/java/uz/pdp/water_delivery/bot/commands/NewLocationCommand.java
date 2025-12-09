package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.service.BotNavigationService;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.UserBotService;
import uz.pdp.water_delivery.model.enums.TelegramState;

@Service
public class NewLocationCommand implements BotCommand {

    private final String command;
    private final BotService botService;
    private final TelegramBot telegramBot;
    private final UserBotService userBotService;
    private final BotNavigationService botNavigationService;

    public NewLocationCommand(BotService botService, TelegramBot telegramBot, UserBotService userBotService, BotNavigationService botNavigationService) {
        this.command = BotConstant.NEW_LOCATION;
        this.botService = botService;
        this.telegramBot = telegramBot;
        this.userBotService = userBotService;
        this.botNavigationService = botNavigationService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message , TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SETTING)) {
            botNavigationService.sendNewLocationButton(message, telegramUser);
        } else {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
