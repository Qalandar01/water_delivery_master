package uz.pdp.water_delivery.bot.handlers.state;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.DeleteMessageService;

@HandlesState(TelegramState.HAS_ORDER)
@Component
@RequiredArgsConstructor
public class HasOrderHandler implements StateHandler {

    private final DeleteMessageService deleteMessageService;
    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;

    @Override
    public void handle(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.START);
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.HAS_ORDER
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.HAS_ORDER);
        telegramUserRepository.save(telegramUser);
    }
}
