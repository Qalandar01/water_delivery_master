package uz.pdp.water_delivery.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.water_delivery.entity.BotConstant;
import uz.pdp.water_delivery.repo.BotConstantRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BotConstantService {

    @Autowired
    private BotConstantRepository botConstantRepository;

    private final Map<String, String> constantsCache = new HashMap<>();

    public String getConstant(String key) {
        System.out.println("Received key: " + key); // Debug uchun

        if (constantsCache.containsKey(key)) {
            System.out.println("Cachedan topildi: " + constantsCache.get(key));
            return constantsCache.get(key);
        }

        Optional<BotConstant> botConstant = botConstantRepository.findByConstantKey(key);
        if (botConstant.isPresent()) {
            String value = botConstant.get().getConstantValue();
            System.out.println("DB'dan topildi: " + value);
            constantsCache.put(key, value);
            return value;
        }

        System.out.println("Key topilmadi: " + key);
        return null;
    }

    public void clearCache() {
        constantsCache.clear();
    }
}
