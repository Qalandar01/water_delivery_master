package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;

@Service
public class OrderBtnCommand implements BotCommand {

    private final String command;
    private final BotService botService;

    public OrderBtnCommand(BotService botService) {
        this.command = BotConstant.ORDER_BTN;
        this.botService = botService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        botService.startOrdering(message, telegramUser);
    }
}
