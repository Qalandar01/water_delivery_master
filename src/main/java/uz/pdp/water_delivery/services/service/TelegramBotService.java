package uz.pdp.water_delivery.services.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService {

    @Autowired
    private TelegramBot telegramBot;

    public void sendImage(Long chatId, String imageUrl, String description) {
        SendPhoto sendPhoto = new SendPhoto(chatId, imageUrl).caption(description);
        telegramBot.execute(sendPhoto);
    }
}
