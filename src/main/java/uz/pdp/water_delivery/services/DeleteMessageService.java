package uz.pdp.water_delivery.services;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.deletMessage.DeleteMessageForTelegramUser;
import uz.pdp.water_delivery.model.repo.DeleteMessageForTelegramUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteMessageService {

    private final DeleteMessageForTelegramUserRepository deleteMessageRepository;

    public void archivedForDeletingMessages(TelegramUser telegramUser, Integer messageId, String message) {
        DeleteMessageForTelegramUser deleteMessage = DeleteMessageForTelegramUser.builder()
                .chatId(telegramUser.getChatId())
                .message(message)
                .messageId(messageId)
                .build();
        deleteMessageRepository.save(deleteMessage);
    }

    @Transactional
    public void deleteMessageAll(TelegramBot telegramBot, TelegramUser telegramUser) {
        List<DeleteMessageForTelegramUser> deleteMessageForTelegramUsers = deleteMessageRepository.findAllByChatId(telegramUser.getChatId());
        for (DeleteMessageForTelegramUser deleteMessageForTelegramUser : deleteMessageForTelegramUsers) {
            telegramBot.execute(new DeleteMessage(deleteMessageForTelegramUser.getChatId(), deleteMessageForTelegramUser.getMessageId()));
        }
        deleteMessageRepository.deleteAllByChatId(telegramUser.getChatId());
    }

    @Async
    public void deleteMessageBasket(TelegramBot telegramBot, TelegramUser telegramUser) {
        try {
            List<DeleteMessageForTelegramUser> messages = findAllMessagesForUser(telegramUser);
            for (DeleteMessageForTelegramUser message : messages) {
                telegramBot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }

    public List<DeleteMessageForTelegramUser> findAllMessagesForUser(TelegramUser telegramUser) {
        return deleteMessageRepository.findAllByChatId(telegramUser.getChatId());
    }
}
