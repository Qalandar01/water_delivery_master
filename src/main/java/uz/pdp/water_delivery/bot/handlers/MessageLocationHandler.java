package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;

@Service
@RequiredArgsConstructor
public class MessageLocationHandler implements UpdateHandler {

    private final BotService botService;
    private final BotDelivery botDelivery;
    private final TelegramBot telegramBot;

    @Override
    public boolean canHandle(Update update) {
        return update.message()!=null && update.message().location()!=null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        Message message = update.message();
        TelegramUser telegramUser = botService.getTelegramUserOrCreate(message.chat().id());


        switch (telegramUser.getState()) {

            case SHARE_LOCATION -> botService.saveLocationSendMessage(message, telegramUser);

            case SHARE_LOCATION_DELIVERY -> botDelivery.saveLocationDeliverySendMessage(message, telegramUser);

            case SAVE_NEW_LOCATION -> botService.saveNewLocation(message, telegramUser);

            default -> telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
