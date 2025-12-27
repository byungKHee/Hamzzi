package hamzzi.uci.factory;

import hamzzi.uci.command.UciCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

    private final Map<String, UciCommand> commands = new HashMap<>();

    public CommandFactory() {
//        commands.put("uci", new UciCommandUci());
        commands.put("isready", (ctx, arg) -> ctx.send("readyok"));
//        commands.put("position", new PositionCommand());
//        commands.put("go", new GoCommand());
        commands.put("quit", (ctx, arg) -> ctx.quit());
    }

    public UciCommand getCommand(String command) {
        String cmdKey = command.split(" ")[0];
        return commands.getOrDefault(cmdKey, (ctx, arg) -> {});
    }
}
