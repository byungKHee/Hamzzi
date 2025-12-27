package hamzzi.uci.command;

import hamzzi.uci.core.EngineContext;

public interface UciCommand {
    void execute(EngineContext context, String args);
}
