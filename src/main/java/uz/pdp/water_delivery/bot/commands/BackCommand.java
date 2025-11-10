package uz.pdp.water_delivery.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotServiceIn;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.enums.TelegramState;
import uz.pdp.water_delivery.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.service.DeleteMessageService;

@Service
public class BackCommand implements BotCommand {

    private final String command;
    private final TelegramUserRepository telegramUserRepository;
    private final DeleteMessageService deleteMessageService;
    private final BotServiceIn botServiceIn;

    public BackCommand(TelegramUserRepository telegramUserRepository, DeleteMessageService deleteMessageService, BotServiceIn botServiceIn) {
        this.command = BotConstant.BACK;
        this.telegramUserRepository = telegramUserRepository;
        this.deleteMessageService = deleteMessageService;
        this.botServiceIn = botServiceIn;
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
            botServiceIn.sendCabinet(telegramUser);
        } else if (telegramUser.getState().equals(TelegramState.SAVE_NEW_LOCATION)) {
            telegramUser.setState(TelegramState.SETTING);
            telegramUserRepository.save(telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.BACK);
            botServiceIn.setting(message, telegramUser);
        }
    }
}
