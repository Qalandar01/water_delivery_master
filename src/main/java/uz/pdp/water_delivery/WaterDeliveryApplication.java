package uz.pdp.water_delivery;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories
@EnableJdbcHttpSession
public class WaterDeliveryApplication {

    @Value("${bot.token}")
    private String token;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
        SpringApplication.run(WaterDeliveryApplication.class, args);
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    public Gson gson() {
        return new Gson();
    }

}
