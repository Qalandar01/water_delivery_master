package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.repo.TelegramUserRepository;

@Service
public class CallbackHandler implements UpdateHandler {
    private final BotServiceIn botServiceIn;
    private final TelegramUserRepository telegramUserRepository;
    private final BotDelivery botDelivery;

    public CallbackHandler(BotServiceIn botServiceIn, TelegramUserRepository telegramUserRepository, BotDelivery botDelivery) {
        this.botServiceIn = botServiceIn;
        this.telegramUserRepository = telegramUserRepository;
        this.botDelivery = botDelivery;
    }

    @Override
    public boolean canHandle(Update update) {
        return update.callbackQuery()!=null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        TelegramUser telegramUser = botServiceIn.getTelegramUserOrCreate(callbackQuery.from().id());
        String data = update.callbackQuery().data();
        handleUserCallbackQuery(callbackQuery, telegramUser, data);
    }

    private void handleUserCallbackQuery(CallbackQuery message, TelegramUser telegramUser, String data) {
        switch (telegramUser.getState()) {
            case SELECT_BOTTLE_NUMBER -> botServiceIn.changeBottleNumber(message, telegramUser);

            case CONFIRM_ORDER -> botServiceIn.acceptOrderTimeAndShowConfirmation(message, telegramUser);

            case CREATE_ORDER -> botServiceIn.makeAnOrder(message, telegramUser);

            case MY_BASKET -> {
                if (data.equals(BotConstant.CONFIRM_ORDER)) {
//                    botServiceIn.showDeliveryTimeMenu(message, telegramUser,data);
//                    botServiceIn.deleteBasket(data, telegramUser);
                    botServiceIn.acceptOrderTimeAndShowConfirmation(message, telegramUser);
                } else if (data.equals(BotConstant.CABINET)) {
                    telegramUser.setState(TelegramState.CABINET);
                    telegramUserRepository.save(telegramUser);
                    botServiceIn.sendCabinet(telegramUser);
                }else if(data.startsWith(BotConstant.DELETE)){
                    botServiceIn.removeBasketProduct(telegramUser, data);
                }

            }

            // Delivery
            case START_DELIVERY -> botDelivery.startDeliveryAndShareOrders(telegramUser);

            case CHOOSE_DELIVERY_TIME -> botDelivery.chooseDeliveryTime(message, telegramUser);

            case START_DELIVERED -> botDelivery.shareLocationDelivery(message, telegramUser);

            case DELIVERY_STARTED -> botDelivery.startedDelivery(message, telegramUser);
        }

    }

}
