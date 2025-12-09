package uz.pdp.water_delivery.bot.handlers.state;

import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.UserBotService;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.services.DeleteMessageService;

@HandlesState(TelegramState.HAS_ORDER)
@Component
@RequiredArgsConstructor
public class WaitingOperatorChangeLocationHandler implements StateHandler {
    private final DeleteMessageService deleteMessageService;
    private final BotService botService;
    private final UserBotService userBotService;

    @Override
    public void handle(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.PLEASE_SHARE_CONTACT);
        userBotService.sendDoneMessage(telegramUser);
    }
}
