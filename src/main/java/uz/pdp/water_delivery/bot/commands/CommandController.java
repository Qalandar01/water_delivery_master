package uz.pdp.water_delivery.bot.commands;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandController {

    private final Map<String, BotCommand> map;

    public CommandController(List<BotCommand> commands) {
        map = new HashMap<>();
        for (BotCommand command : commands) {
            map.put(command.getCommand(), command);
        }
    }

    public Map<String, BotCommand> getCommands() {
        return map;
    }

}
