package uz.pdp.water_delivery.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WebhookInitializer implements CommandLineRunner {

    private final TelegramBot telegramBot;

    @Value("${bot.secret_token_for_webhook}")
    private String secret;
    @Value("${bot.webhook_url}")
    String webhookUrl;


    public WebhookInitializer(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void run(String... args) {
        BaseResponse response = telegramBot.execute(new SetWebhook().url(webhookUrl).secretToken(
                secret
        ));
        if (response.isOk()) {
            System.out.println("✅ Webhook set successfully: " + webhookUrl);
        } else {
            System.err.println("❌ Failed to set webhook: " + response.description());
        }
    }
}
