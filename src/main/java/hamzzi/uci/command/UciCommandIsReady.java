package hamzzi.uci.command;

import hamzzi.uci.core.EngineContext;

/**
 * 'isReady' 명령어에 대응하는 클래스
 * The isready command must be answered with the readyok command as soon as the engine is able to process input.
 */
public class UciCommandIsReady implements UciCommand {

    @Override
    public void execute(EngineContext context, String args){
        context.send("readyok");

        context.log("UCI ready complete");
    }
}
