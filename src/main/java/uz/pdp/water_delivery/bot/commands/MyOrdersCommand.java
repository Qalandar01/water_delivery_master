package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.OrderBotService;
import uz.pdp.water_delivery.model.enums.TelegramState;

@Service
public class MyOrdersCommand implements BotCommand {

    private final String command;
    private final TelegramBot telegramBot;
    private final OrderBotService orderBotService;

    public MyOrdersCommand(TelegramBot telegramBot, OrderBotService orderBotService) {
        this.command = BotConstant.MY_ORDERS;
        this.telegramBot = telegramBot;
        this.orderBotService = orderBotService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SETTING)) {
            orderBotService.sendMyOrders(message, telegramUser);
        } else {
            telegramUser.deleteMessage(telegramBot, message.messageId());
        }
    }
}
