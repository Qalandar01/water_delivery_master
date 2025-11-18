package uz.pdp.water_delivery.bot.handlers;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.commands.BotCommand;
import uz.pdp.water_delivery.bot.commands.CommandController;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageTextHandler implements UpdateHandler {

    private final BotService botService;
    private final CommandController commandController;


    @Override
    public boolean canHandle(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    @Transactional
    @Override
    public void handle(Update update) {
        TelegramUser telegramUser = botService.getTelegramUserOrCreate(update.message().chat().id());
        Message message = update.message();
        String text = message.text();

        Map<String, BotCommand> commands = commandController.getCommands();
        BotCommand botCommand = commands.get(text);
        if (botCommand != null) {
            botCommand.execute(message, telegramUser);
        } else {
            switch (telegramUser.getState()) {

                case ENTER_PASSWORD_DELIVERY -> botService.sendCabinetDelivery(message, telegramUser);

                case ENTER_PASSWORD_DELIVERY_CONFIRM -> botService.sendCabinetConfirmCode(message, telegramUser);

                case ENTER_OLD_PASSWORD_DELIVERY -> botService.sendCabinetOldPassword(message, telegramUser);

                case WAITING_OPERATOR_CHANGE_LOCATION -> botService.sendPleaseWaitingOperator(message, telegramUser);

                case SELECT_BOTTLE_TYPE -> botService.acceptBottleTypeShowSelectNumber(message, telegramUser);

                case SETTING -> botService.settingMenu(message, telegramUser);

            }
        }
    }

}
