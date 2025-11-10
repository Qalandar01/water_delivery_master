package uz.pdp.water_delivery.controller;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.service.TelegramBotService;

import java.util.List;
import java.util.UUID;

@Controller
public class TelegramImageController {

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    @Autowired
    private TelegramBotService telegramBotService;

    private String imageUrl;
    private String description;

    @GetMapping("/operator/send-posts")
    public String sendPosts() {
        return "operator/send-posts";
    }

    @PostMapping("/send-image")
    public String sendImage(@RequestParam("image") MultipartFile image,
                            @RequestParam("description") String description) {
        imageUrl = saveImage(image);
        this.description = description;
        sendImagesToUsersAsync();
        return "redirect:/operator";
    }

    @Async
    @SneakyThrows
    public void sendImagesToUsersAsync() {
        List<TelegramUser> users = telegramUserRepository.findAll();
        for (int i = 0; i < users.size(); i += 15) {
            List<TelegramUser> batch = users.subList(i, Math.min(i + 15, users.size()));
            batch.forEach(user ->
                    telegramBotService.sendImage(user.getChatId(), imageUrl, description));
            Thread.sleep(5000);
        }
    }

    @SneakyThrows
    private String saveImage(MultipartFile image) {
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "_" + image.getOriginalFilename();
        String filePath = "/Users/rahmatillojuraev/IdeaProjects/water_delivery/file/photos/" + fileName;
        image.transferTo(new java.io.File(filePath));
        return fileName;
    }

}
