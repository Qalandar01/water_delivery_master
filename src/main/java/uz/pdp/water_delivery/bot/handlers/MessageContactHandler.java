package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.service.UserBotService;
import uz.pdp.water_delivery.model.enums.TelegramState;

@Service
@RequiredArgsConstructor
public class MessageContactHandler implements UpdateHandler {

    private final BotService botService;
    private final UserBotService userBotService;

    @Override
    public boolean canHandle(Update update) {
        return update.message()!=null && update.message().contact()!=null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        Message message = update.message();
        TelegramUser telegramUser = botService.getTelegramUserOrCreate(message.chat().id());
        if (telegramUser.getState().equals(TelegramState.SHARE_CONTACT)) {
            userBotService.saveContactSendMessage(message, telegramUser);
        }
    }
}
