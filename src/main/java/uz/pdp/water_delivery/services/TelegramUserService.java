package uz.pdp.water_delivery.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.water_delivery.bot.service.BotNavigationService;
import uz.pdp.water_delivery.bot.service.BotService;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.model.dto.Location;
import uz.pdp.water_delivery.model.dto.VerifyUserDTO;
import uz.pdp.water_delivery.model.enums.TelegramState;
import uz.pdp.water_delivery.exception.TelegramUserNotFoundException;
import uz.pdp.water_delivery.exception.UserDeletionException;
import uz.pdp.water_delivery.projection.SimpleWaitingUser;
import uz.pdp.water_delivery.model.repo.TelegramUserRepository;
import uz.pdp.water_delivery.utils.LogErrorFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramUserService {
    private final TelegramUserRepository telegramUserRepository;
    private final LogErrorFile logErrorFile;
    private final BotService botService;
    private final BotNavigationService botNavigationService;

    public TelegramUser getUserById(Long id) {
        return telegramUserRepository.findById(id)
                .orElseThrow(() -> {
                    TelegramUserNotFoundException ex = new TelegramUserNotFoundException(id);
                    logErrorFile.logError(ex, "getUserById", id);
                    return ex;
                });
    }

    @Transactional
    public void verifyUser(VerifyUserDTO dto) {
        TelegramUser tgUser = telegramUserRepository.findById(dto.getTgUserId())
                .orElseThrow(() -> new TelegramUserNotFoundException(dto.getTgUserId()));

        // Update basic info
        tgUser.setLocation(new Location(dto.getLatitude(), dto.getLongitude()));
        tgUser.setAddressLine(dto.getAddressLine());
        tgUser.setIsHome(dto.isHome());
        tgUser.setVerified(true);
        tgUser.setPhoneOff(false);
        tgUser.setState(TelegramState.CABINET);
        tgUser.setChangeLocation(false);

        // Home-specific info
        if (!dto.isHome()) {
            tgUser.setXonadon(dto.getXonadon());
            tgUser.setPodyez(dto.getPodyez());
            tgUser.setQavat(dto.getQavat());
            tgUser.setKvRaqami(dto.getKvRaqami());
        }

        // Update phone if provided
        if (dto.getPhone() != null) {
            tgUser.getUser().setDoublePhone(dto.getPhone());
        }

        telegramUserRepository.save(tgUser);

        // Notify user
        botNavigationService.sendCabinet(tgUser);
    }

    @Transactional
    public void handleNoPhone(VerifyUserDTO dto) {
        TelegramUser tgUser = telegramUserRepository.findById(dto.getTgUserId())
                .orElseThrow(() -> new TelegramUserNotFoundException(dto.getTgUserId()));

        // Update location info if provided
        if (dto.getLongitude() != null || dto.getLatitude() != null ||
                dto.getAddressLine() != null || dto.getDistrict() != null) {
            tgUser.setLocation(new Location(dto.getLatitude(), dto.getLongitude()));
            tgUser.setAddressLine(dto.getAddressLine());
            tgUser.setDistrict(dto.getDistrict());
        }

        // Update phone state
        tgUser.setPhoneOff(true);
        tgUser.setState(TelegramState.NO_PHONE);

        telegramUserRepository.save(tgUser);

        // Notify user
        botNavigationService.sendUserDidNotAnswerPhone(tgUser);
    }

    public List<TelegramUser> getAllUsersWithRoleUser() {
        return telegramUserRepository.findAllByRoleNameUsers();
    }

    public List<TelegramUser> searchUsersByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return telegramUserRepository.findAll();
        }
        return telegramUserRepository.searchByUserPhone(keyword);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        TelegramUser user = telegramUserRepository.findById(userId)
                .orElseThrow(() -> new TelegramUserNotFoundException(userId));

        try {
            user.setIsDeleted(true);
            telegramUserRepository.save(user);
        } catch (Exception e) {
            throw new UserDeletionException("Failed to delete user due to related data.", e);
        }
    }

    public List<TelegramUser> getUsersWithPhoneOff() {
        return telegramUserRepository.findAllByPhoneOff();
    }

    public List<SimpleWaitingUser> getUsersRequestingLocationChange() {
        return telegramUserRepository.findAllByChangeLocation(true);
    }
}
