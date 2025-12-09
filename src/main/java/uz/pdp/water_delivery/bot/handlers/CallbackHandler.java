package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.service.BasketBotService;
import uz.pdp.water_delivery.bot.service.BotNavigationService;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;
import uz.pdp.water_delivery.bot.service.OrderBotService;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;

@Service
public class CallbackHandler implements UpdateHandler {
    private final BotService botService;
    private final TelegramUserRepository telegramUserRepository;
    private final BotDelivery botDelivery;
    private final BasketBotService basketBotService;
    private final BotNavigationService botNavigationService;
    private final OrderBotService orderBotService;

    public CallbackHandler(BotService botService, TelegramUserRepository telegramUserRepository, BotDelivery botDelivery, BasketBotService basketBotService, BotNavigationService botNavigationService, OrderBotService orderBotService) {
        this.botService = botService;
        this.telegramUserRepository = telegramUserRepository;
        this.botDelivery = botDelivery;
        this.basketBotService = basketBotService;
        this.botNavigationService = botNavigationService;
        this.orderBotService = orderBotService;
    }

    @Override
    public boolean canHandle(Update update) {
        return update.callbackQuery()!=null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        TelegramUser telegramUser = botService.getTelegramUserOrCreate(callbackQuery.from().id());
        String data = update.callbackQuery().data();
        handleUserCallbackQuery(callbackQuery, telegramUser, data);
    }

    private void handleUserCallbackQuery(CallbackQuery message, TelegramUser telegramUser, String data) {
        switch (telegramUser.getState()) {
            case SELECT_BOTTLE_NUMBER -> basketBotService.changeProductNumber(message, telegramUser);

            case CONFIRM_ORDER -> botService.acceptOrderTimeAndShowConfirmation(message, telegramUser);

            case CREATE_ORDER -> orderBotService.makeAnOrder(message, telegramUser);

            case MY_BASKET -> {
                if (data.equals(BotConstant.CONFIRM_ORDER)) {
//                    botService.showDeliveryTimeMenu(message, telegramUser,data);
//                    botService.deleteBasket(data, telegramUser);
                    botService.acceptOrderTimeAndShowConfirmation(message, telegramUser);
                } else if (data.equals(BotConstant.CABINET)) {
                    telegramUser.setState(TelegramState.CABINET);
                    telegramUserRepository.save(telegramUser);
                    botNavigationService.sendCabinet(telegramUser);
                }else if(data.startsWith(BotConstant.DELETE)){
                    basketBotService.removeBasketProduct(telegramUser, data);
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
