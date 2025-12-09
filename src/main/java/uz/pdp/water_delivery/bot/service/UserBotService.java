package uz.pdp.water_delivery.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.BotConstant;
import uz.pdp.water_delivery.bot.BotUtils;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.dto.Location;
import uz.pdp.water_delivery.model.entity.Order;
import uz.pdp.water_delivery.model.entity.User;
import uz.pdp.water_delivery.model.enums.RoleName;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.model.repo.OrderRepository;
import uz.pdp.water_delivery.model.repo.RoleRepository;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.model.repo.UserRepository;
import uz.pdp.water_delivery.services.DeleteMessageService;
import uz.pdp.water_delivery.services.UserService;
import uz.pdp.water_delivery.utils.PhoneRepairUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBotService {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeleteMessageService deleteMessageService;
    private final UserService userService;
    private final BotUtils botUtils;
    private final OrderRepository orderRepository;

    @Transactional
    public TelegramUser getTelegramUserOrCreate(Long chatId) {
        try {
            return telegramUserRepository.findByChatId(chatId).orElseGet(() -> {
                TelegramUser newUser = new TelegramUser(chatId);
                return telegramUserRepository.saveAndFlush(newUser); // Use saveAndFlush
            });
        } catch (DataIntegrityViolationException e) {
            // If another thread created it, fetch it
            return telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new RuntimeException("Failed to get or create user"));
        }
    }

    public void acceptStartSendShareContact(Message message, TelegramUser telegramUser) {
        if (telegramUser.getState() == null) {
            telegramUser.setState(TelegramState.START);
            telegramUserRepository.save(telegramUser);
        }

        if (telegramUser.getState().equals(TelegramState.HAS_ORDER)) {
            // leave behavior to caller/orchestrator
            return;
        } else if (telegramUser.getState().equals(TelegramState.WAITING_OPERATOR)
                || telegramUser.getState().equals(TelegramState.WAITING_OPERATOR_CHANGE_LOCATION)) {
            return;
        } else if (telegramUser.getState().equals(TelegramState.SHARE_LOCATION)) {
            return;
        } else if (telegramUser.getUser() != null && telegramUser.getUser().getPhone() != null) {
            if (isDeliveryUser(telegramUser.getUser())) {
                handleDeliveryUser(message, telegramUser);
            } else if (isUser(telegramUser.getUser())) {
                telegramUser.setState(TelegramState.CABINET);
                telegramUserRepository.save(telegramUser);
                // navigation (sendCabinet) should be handled by BotNavigationService or BotService orchestrator
            }
        } else {
            SendMessage sendMessage = new SendMessage(telegramUser.getChatId(), BotConstant.PLEASE_SHARE_CONTACT);
            sendMessage.replyMarkup(BotUtils.getGeneratedContactButton());
            SendResponse sendResponse = telegramBot.execute(sendMessage);
            Integer messageId = sendResponse.message().messageId();
            deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), BotConstant.START);
            deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.PLEASE_SHARE_CONTACT);
            telegramUser.setState(TelegramState.SHARE_CONTACT);
            telegramUserRepository.save(telegramUser);
        }
    }

    public void saveContactSendMessage(Message message, TelegramUser telegramUser) {
        String contact = PhoneRepairUtil.repair(message.contact().phoneNumber());
        User user = userService.createdOrFindUser(contact);
        telegramUser.setRegion("Toshkent");
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
        if (user != null) {
            user.setFirstName(message.contact().firstName());
            user.setLastName(message.contact().lastName());
            telegramUser.setUser(user);
            telegramUserRepository.save(telegramUser);
            userRepository.save(user);
        }
        if (telegramUser.getUser() != null && isDeliveryUser(telegramUser.getUser())) {
            handleDeliveryUser(message, telegramUser);
        } else if (telegramUser.getUser() != null && isUser(telegramUser.getUser())) {
            telegramUser.setState(TelegramState.CABINET);
            telegramUser.getUser().setNewUser(false);
            telegramUserRepository.save(telegramUser);
            // navigation (sendCabinet) should be handled by orchestrator
        } else {
            handleRegularUser(message, telegramUser, contact);
        }
    }

    private boolean isUser(User user) {
        return user != null &&
                user.getRoles() != null &&
                user.getRoles().stream()
                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_USER));
    }

    private boolean isDeliveryUser(User user) {
        return user != null &&
                user.getRoles() != null &&
                user.getRoles().stream()
                        .anyMatch(role -> role.getRoleName().equals(RoleName.ROLE_DELIVERY));
    }

    private void handleDeliveryUser(Message message, TelegramUser telegramUser) {
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share contact");
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                telegramUser.getUser().getPassword() != null ? BotConstant.PLEASE_ENTER_PASSWORD : BotConstant.NEW_PASSWORD
        );
        sendMessage.replyMarkup(new ReplyKeyboardRemove(true));
        telegramUser.setState(telegramUser.getUser().getPassword() != null ? TelegramState.ENTER_OLD_PASSWORD_DELIVERY : TelegramState.ENTER_PASSWORD_DELIVERY);
        telegramUserRepository.save(telegramUser);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, telegramUser.getUser().getPassword() != null ? BotConstant.PLEASE_ENTER_PASSWORD : BotConstant.NEW_PASSWORD);
    }

    private void handleRegularUser(Message message, TelegramUser telegramUser, String contact) {
        if (telegramUser.getUser() == null) {
            telegramUser.setUser(new User());
        }
        telegramUser.getUser().setPhone(contact);
        telegramUser.setUser(telegramUser.getUser());
        sendLocationButton(telegramUser);
    }

    public void sendLocationButton(TelegramUser tgUser) {
        SendMessage message = new SendMessage(tgUser.getChatId(), BotConstant.PLEASE_SHARE_LOCATION);
        message.replyMarkup(botUtils.getGeneratedLocationButton());
        SendResponse sendResponse = telegramBot.execute(message);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(tgUser, messageId, BotConstant.PLEASE_SHARE_LOCATION);
        tgUser.setState(TelegramState.SHARE_LOCATION);
        if (checkUser(tgUser)) {
            if (tgUser.getUser() != null) tgUser.getUser().setNewUser(true);
        }
        if (tgUser.getUser() != null) {
            tgUser.getUser().setRoles(List.of(roleRepository.findByRoleName(RoleName.ROLE_USER)));
        }
        telegramUserRepository.save(tgUser);
    }

    private boolean checkUser(TelegramUser tgUser) {
        List<Order> order = orderRepository.findAllByTelegramUser(tgUser);
        return order.isEmpty();
    }


    public void saveLocationSendMessage(Message message, TelegramUser telegramUser) {
        ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove(true);
        telegramUser.setLocation(new Location(message.location().latitude().doubleValue(), message.location().longitude().doubleValue()));
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "Please share location");
        sendDoneMessage(telegramUser, removeKeyboard);
        telegramUser.setState(TelegramState.WAITING_OPERATOR);
        telegramUserRepository.save(telegramUser);
    }
    private void sendDoneMessage(TelegramUser telegramUser, ReplyKeyboardRemove removeKeyboard) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                uz.pdp.water_delivery.bot.BotConstant.DONE
        );
        sendMessage.replyMarkup(removeKeyboard);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Location received");
    }
    public void sendDoneMessage(TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                uz.pdp.water_delivery.bot.BotConstant.DONE_ALREADY
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, "Location received");
    }
    public void saveNewLocation(Message message, TelegramUser telegramUser) {
        Location location = new Location(message.location().latitude().doubleValue(), message.location().longitude().doubleValue());
        telegramUser.setLocation(location);
        telegramUserRepository.save(telegramUser);
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                BotConstant.LOCATION_SAVED
        );
        sendMessage.replyMarkup(botUtils.getGeneratedSettingButtons());
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Integer messageId = sendResponse.message().messageId();
        deleteMessageService.archivedForDeletingMessages(telegramUser, message.messageId(), "New location saved");
        deleteMessageService.archivedForDeletingMessages(telegramUser, messageId, BotConstant.LOCATION_SAVED);
        telegramUser.setState(TelegramState.WAITING_OPERATOR_CHANGE_LOCATION);
        telegramUser.setChangeLocation(true);
        telegramUserRepository.save(telegramUser);
    }
}
