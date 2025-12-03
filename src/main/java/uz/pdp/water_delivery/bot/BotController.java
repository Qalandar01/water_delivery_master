package uz.pdp.water_delivery.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
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
        if (update.callbackQuery() != null) {
            handleUnmatchedCallback(update.callbackQuery());
            return;
        }

        Message message = update.message();

        if (message == null) {
            throw new UnsupportedOperationException(
                    "Update has no message and no callbackQuery: " + update
            );
        }
        handleUnmatchedMessage(message);
    }

    private void handleUnmatchedMessage(Message message) {
        Chat chat = message.chat();
        if (chat == null) {
            throw new IllegalStateException("Message has no chat: " + message);
        }

        long chatId = chat.id();
        log.info("No handler found for message from chatId={}", chatId);

        TelegramUser telegramUser = botService.getTelegramUserOrCreate(chatId);
        if (telegramUser == null) {
            throw new IllegalStateException("Could not load/create TelegramUser for chatId=" + chatId);
        }

        try {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        } catch (Exception e) {
            log.error("Failed to delete message {} for chatId={}", message.messageId(), chatId, e);
        }
    }
    private void handleUnmatchedCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.from().id();
        log.warn("No handler found for callback data='{}' from chatId={}",
                callbackQuery.data(), chatId);
        telegramBot.execute(new AnswerCallbackQuery(callbackQuery.id()));
    }


}
