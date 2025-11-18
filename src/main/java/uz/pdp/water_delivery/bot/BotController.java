package uz.pdp.water_delivery.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.handlers.UpdateHandler;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotController {

    private final List<UpdateHandler> updateHandlers;
    private final BotService botService;
    private final TelegramBot telegramBot;

    @Async
    public void handleUpdate(Update update) {
        try {
            updateHandlers.stream()
                    .filter(handler -> handler.canHandle(update))
                    .findFirst()
                    .ifPresentOrElse(
                            handler -> handler.handle(update),
                            () -> noHandlerFound(update)
                    );
        } catch (Exception e) {
            log.error("Error handling update: {}", update, e);
        }
    }

    private void noHandlerFound(Update update) {
        Message message = update.message();
        if (message == null || message.chat() == null) {
            log.warn("Unhandled update without message: {}", update);
            return;
        }

        log.info("No handler found for message from chatId={}", message.chat().id());

        var telegramUser = botService.getTelegramUserOrCreate(message.chat().id());
        if (telegramUser != null) {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
