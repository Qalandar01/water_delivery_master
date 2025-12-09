package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.service.OrderBotService;

@Service
public class OrderBtnCommand implements BotCommand {

    private final String command;
    private final OrderBotService orderBotService;

    public OrderBtnCommand( OrderBotService orderBotService) {
        this.command = BotConstant.ORDER_BTN;
        this.orderBotService = orderBotService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        orderBotService.startOrdering(message, telegramUser);
    }
}
