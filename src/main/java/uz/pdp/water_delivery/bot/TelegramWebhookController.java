package uz.pdp.water_delivery.bot;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final Gson gson;
    private final BotController botController;

    @Value("${bot.secret_token_for_webhook}")
    private String secret;

    @PostMapping
    public ResponseEntity<?> onUpdate(
            @RequestBody String rawJson,
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String token
    ) {
        System.out.println("=== WEBHOOK RECEIVED ===");
        System.out.println("Raw JSON: " + rawJson);
        System.out.println("Token received: " + token);
        System.out.println("Token expected: " + secret);
        System.out.println("Token matches: " + token.equals(secret));

        Update update = gson.fromJson(rawJson, Update.class);
        System.out.println("Update parsed: " + update);
        System.out.println("Update ID: " + (update != null ? update.updateId() : "NULL"));
        if (token.equals(secret)) {
            botController.handleUpdate(update);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
