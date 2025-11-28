package uz.pdp.water_delivery.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.services.TelegramBotService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TelegramImageController {

    private TelegramUserRepository telegramUserRepository;

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
