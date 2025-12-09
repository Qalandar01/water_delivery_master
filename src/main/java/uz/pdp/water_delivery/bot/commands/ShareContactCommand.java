package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.service.UserBotService;

@Service
public class ShareContactCommand implements BotCommand {

    private final String command;
    private final BotService botService;
    private final UserBotService userBotService;

    public ShareContactCommand(BotService botService, UserBotService userBotService) {
        this.command = BotConstant.SHARE_CONTACT;
        this.botService = botService;
        this.userBotService = userBotService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        userBotService.saveContactSendMessage(message, telegramUser);
    }
}
