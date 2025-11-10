package uz.pdp.water_delivery.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.handlers.UpdateHandler;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotController {

    private final List<UpdateHandler> updateHandlers;
    private final BotServiceIn botServiceIn;
    private final TelegramBot telegramBot;

    @Async
    public void handleUpdate(Update update) {
        updateHandlers.stream()
                .filter(h -> h.canHandle(update))
                .findFirst()
                .ifPresentOrElse(
                        h -> h.handle(update),
                        () -> noHandlerFound(update)
                );

    }

    private void noHandlerFound(Update update) {
        Message message = update.message();
        TelegramUser telegramUser = botServiceIn.getTelegramUserOrCreate(message.chat().id());
        telegramUser.deleteMessage(telegramBot, message.messageId());
    }


}
