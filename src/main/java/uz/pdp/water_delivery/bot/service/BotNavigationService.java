package uz.pdp.water_delivery.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.bot.delivery.BotDelivery;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.model.repo.UserRepository;
import uz.pdp.water_delivery.services.DeleteMessageService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotNavigationService {

    private final TelegramBot bot;
    private final DeleteMessageService deleteMessageService;
    private final BotUtils botUtils;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BotService botService;
    private final BotDelivery botDelivery;

    public void sendCabinet(Message message, TelegramUser tgUser) {
        if (!tgUser.getState().equals(TelegramState.CABINET)) {
            deleteMessageService.archivedForDeletingMessages(tgUser, message.messageId(), "Your cabinet will be here");
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), uz.pdp.water_delivery.bot.BotConstant.MENU);
            sendMessage.replyMarkup(botUtils.getGeneratedCabinetButtons());
            SendResponse sendResponse = bot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(bot, tgUser);
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, uz.pdp.water_delivery.bot.BotConstant.MENU);
            tgUser.setState(TelegramState.START_ORDERING);
            telegramUserRepository.save(tgUser);
        }
    }
    public void sendCabinet(TelegramUser tgUser) {
        if (tgUser.getState().equals(TelegramState.CABINET)) {
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), uz.pdp.water_delivery.bot.BotConstant.MENU);
            sendMessage.replyMarkup(botUtils.getGeneratedCabinetButtons());
            SendResponse sendResponse = bot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(bot, tgUser);
            deleteMessageService.archivedForDeletingMessages(tgUser, messageId, uz.pdp.water_delivery.bot.BotConstant.MENU);
            tgUser.setState(TelegramState.START_ORDERING);
            telegramUserRepository.save(tgUser);
        }
    }
    public void sendCabinetDelivery(Message message, TelegramUser telegramUser) {
        User user = getOrCreateUser(telegramUser);
        user.setPassword(message.text());
        telegramUser.setState(TelegramState.ENTER_PASSWORD_DELIVERY_CONFIRM);
        botService.sendMessage(telegramUser, BotConstant.CONFIRM_PASSWORD);
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.CONFIRM_PASSWORD);
        telegramUserRepository.save(telegramUser);
    }
    private User getOrCreateUser(TelegramUser telegramUser) {
        Optional<User> user = userRepository.findByPhone(telegramUser.getUser().getPhone());
        if (user.isEmpty()) {
            user = Optional.ofNullable((telegramUser.getUser()));
        }
        return user.orElse(null);
    }
    public void askLocation(Long chatId) {
        bot.execute(new SendMessage(chatId, "📍 Please send your location"));
    }
    public void sendCabinetConfirmCode(Message message, TelegramUser telegramUser) {
        if (message.text().equals(telegramUser.getUser().getPassword())) {
            telegramUser.setState(TelegramState.START_DELIVERY);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.CONFIRM_PASSWORD);
            telegramUserRepository.save(telegramUser);
            botDelivery.startDelivery(message, telegramUser);
        } else {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    BotConstant.INCORRECT_PASSWORD
            );
            SendResponse sendResponse = bot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.deleteMessageAll(bot, telegramUser);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.INCORRECT_PASSWORD);
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.INCORRECT_PASSWORD);
        }
    }


    public void sendCabinetOldPassword(Message message, TelegramUser telegramUser) {
        if (isPasswordCorrect(message.text(), telegramUser)) {
            telegramUser.getUser().setPassword(passwordEncoder.encode(message.text()));
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.OLD_PASSWORD);
        } else {
            deleteMessageService.deleteMessageAll(bot, telegramUser);
            botService.sendMessage(telegramUser, BotConstant.INCORRECT_PASSWORD);
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.INCORRECT_PASSWORD);
            return;
        }
        botDelivery.deliveryMenu(message, telegramUser);
    }

    private boolean isPasswordCorrect(String inputPassword, TelegramUser telegramUser) {
        return passwordEncoder.matches(inputPassword, telegramUser.getUser().getPassword());
    }



    public void sendUserDidNotAnswerPhone(TelegramUser tgUser) {
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                BotConstant.USER_DID_NOT_ANSWER
        );
        SendResponse sendResponse = bot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.USER_DID_NOT_ANSWER);
    }
    public void sendNewLocationButton(Message message, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.LOCATION
        );
        sendMessage.replyMarkup(botUtils.getChangeLocationButton());
        SendResponse sendResponse = bot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "New location button");
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.LOCATION);
        telegramUser.setState(TelegramState.SAVE_NEW_LOCATION);
        telegramUserRepository.save(telegramUser);
    }


    public void sendPleaseWaitingOperator(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please waiting operator");
        botService.sendMessage(telegramUser, BotConstant.PLEASE_WAITING_OPERATOR);
    }

}
