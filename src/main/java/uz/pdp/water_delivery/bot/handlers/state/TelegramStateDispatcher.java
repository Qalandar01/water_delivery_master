package uz.pdp.water_delivery.bot.handlers.state;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Component;
import uz.pdp.water_delivery.bot.TelegramUser;
import uz.pdp.water_delivery.entity.enums.TelegramState;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramStateDispatcher {

    private final Map<TelegramState, StateHandler> handlers = new EnumMap<>(TelegramState.class);

    public TelegramStateDispatcher(List<StateHandler> beans) {
        for (StateHandler bean : beans) {
            HandlesState ann = bean.getClass().getAnnotation(HandlesState.class);
            if (ann != null) {
                handlers.put(ann.value(), bean);
            }
        }
    }

    public void dispatch(Message message, TelegramUser user) {
        StateHandler handler =
            handlers.getOrDefault(user.getState(), handlers.get(TelegramState.SHARE_CONTACT));
        handler.handle(message, user);
    }
}
