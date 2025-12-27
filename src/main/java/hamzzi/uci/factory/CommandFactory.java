package hamzzi.uci.factory;

import hamzzi.uci.command.UciCommand;
import hamzzi.uci.command.UciCommandIsReady;
import hamzzi.uci.command.UciCommandPosition;
import hamzzi.uci.command.UciCommandUci;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

    private final Map<String, UciCommand> commands = new HashMap<>();

    public CommandFactory() {
        commands.put("uci", new UciCommandUci());
        commands.put("isready", new UciCommandIsReady());
        commands.put("position", new UciCommandPosition());
    }

    public UciCommand getCommand(String command) {
        String cmdKey = command.split(" ")[0];
        return commands.getOrDefault(cmdKey, (ctx, arg) -> {
            ctx.log("Unknown command: " + arg);
        });
    }
}
