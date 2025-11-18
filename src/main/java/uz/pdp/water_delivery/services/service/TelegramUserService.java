package uz.pdp.water_delivery.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.exception.TelegramUserNotFoundException;
import uz.pdp.water_delivery.repo.TelegramUserRepository;
import uz.pdp.water_delivery.utils.LogErrorFile;

@Service
@RequiredArgsConstructor
public class TelegramUserService {
    private final TelegramUserRepository telegramUserRepository;
    private final LogErrorFile logErrorFile;

    public TelegramUser getUserById(Long id) {
        return telegramUserRepository.findById(id)
                .orElseThrow(() -> {
                    TelegramUserNotFoundException ex = new TelegramUserNotFoundException(id);
                    logErrorFile.logError(ex, "getUserById", id);
                    return ex;
                });
    }
}
