package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.UserBotService;

@Service
@RequiredArgsConstructor
public class StartCommand implements BotCommand {
    private final UserBotService botUserService;


    @Override
    public String getCommand() {
        return BotConstant.START;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botUserService.acceptStartSendShareContact(message, telegramUser);
    }
}
