package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.enums.TelegramState;

@Service
@RequiredArgsConstructor
public class MessageContactHandler implements UpdateHandler {

    private final BotServiceIn botServiceIn;

    @Override
    public boolean canHandle(Update update) {
        return update.message()!=null && update.message().contact()!=null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        Message message = update.message();
        TelegramUser telegramUser = botServiceIn.getTelegramUserOrCreate(message.chat().id());
        if (telegramUser.getState().equals(TelegramState.SHARE_CONTACT)) {
            botServiceIn.saveContactSendMessage(message, telegramUser);
        }
    }
}
