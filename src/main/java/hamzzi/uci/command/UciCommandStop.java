package hamzzi.uci.command;

import hamzzi.uci.core.EngineContext;

/**
 * 'stop' 명령어에 대응하는 클래스
 * stop tells the engine to stop calculating as soon as possible,
 * potentially interrupting the search initiated by a previous go command early.
 * If the engine was not searching when it received stop, it should simply ignore it.
 */
public class UciCommandStop implements UciCommand {

    @Override
    public void execute(EngineContext context, String args){
        context.stopSearch();
    }
}
