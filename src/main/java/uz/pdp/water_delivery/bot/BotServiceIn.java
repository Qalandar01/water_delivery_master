package uz.pdp.water_delivery.bot;


import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;

import java.time.LocalDate;

public interface BotServiceIn {

    TelegramUser getTelegramUserOrCreate(Long id);

    void acceptStartSendShareContact(Message message, TelegramUser telegramUser);

    void saveContactSendMessage(Message message, TelegramUser telegramUser);

    void saveLocationSendMessage(Message message, TelegramUser telegramUser);

    void sendLocationButton(TelegramUser tgUser);

    void sendCabinet(Message message, TelegramUser tgUser);

    void sendCabinet(TelegramUser tgUser);

    void startOrdering(Message message, TelegramUser telegramUser);

    void acceptBottleTypeShowSelectNumber(Message message, TelegramUser telegramUser);

    void changeBottleNumber(CallbackQuery message, TelegramUser telegramUser);

    void acceptOrderTimeAndShowConfirmation(CallbackQuery message, TelegramUser telegramUser);

    void makeAnOrder(CallbackQuery message, TelegramUser telegramUser);

//    LocalDate getDate(String day);

    void sendCabinetDelivery(Message message, TelegramUser telegramUser);

    void sendCabinetConfirmCode(Message message, TelegramUser telegramUser);

    void sendCabinetOldPassword(Message message, TelegramUser telegramUser);

    void sendUserDidNotAnswerPhone(TelegramUser tgUser);


    void setting(Message message, TelegramUser telegramUser);

    void saveNewLocation(Message message, TelegramUser telegramUser);

    void sendNewLocationButton(Message message, TelegramUser telegramUser);

    void sendMyOrders(Message message, TelegramUser telegramUser);

    void sendPleaseWaitingOperator(Message message, TelegramUser telegramUser);

    void showBasket(Message message, TelegramUser telegramUser);

    void settingMenu(Message message, TelegramUser telegramUser);

//    void decreaseBasketAmount(Integer uuid, TelegramUser telegramUser);

//    void increaseBasketAmount(Integer basketId, TelegramUser telegramUser);

//    void updateBasketAmount(String data, TelegramUser telegramUser);

    void deleteBasket(String data, TelegramUser telegramUser);

    void showDeliveryTimeMenu(CallbackQuery message, TelegramUser telegramUser, String data);

    void removeBasketProduct(TelegramUser telegramUser, String data);

}
