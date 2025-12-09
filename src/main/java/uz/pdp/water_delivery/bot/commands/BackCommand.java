package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.service.BotNavigationService;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.DeleteMessageService;

@Service
public class BackCommand implements BotCommand {

    private final String command;
    private final TelegramUserRepository telegramUserRepository;
    private final DeleteMessageService deleteMessageService;
    private final BotService botService;
    private final BotNavigationService botNavigationService;

    public BackCommand(TelegramUserRepository telegramUserRepository, DeleteMessageService deleteMessageService, BotService botService, BotNavigationService botNavigationService) {
        this.command = BotConstant.BACK;
        this.telegramUserRepository = telegramUserRepository;
        this.deleteMessageService = deleteMessageService;
        this.botService = botService;
        this.botNavigationService = botNavigationService;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void execute(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState().equals(TelegramState.SELECT_BOTTLE_TYPE)) {
            telegramUser.setState(TelegramState.CABINET);
            telegramUserRepository.save(telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.BACK);
            botNavigationService.sendCabinet(telegramUser);
        } else if (telegramUser.getState().equals(TelegramState.SAVE_NEW_LOCATION)) {
            telegramUser.setState(TelegramState.SETTING);
            telegramUserRepository.save(telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.BACK);
            botService.setting(message, telegramUser);
        }
    }
}
