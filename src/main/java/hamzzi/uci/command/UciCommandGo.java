package hamzzi.uci.command;

import hamzzi.engine.searcher.SearchListener;
import hamzzi.uci.core.EngineContext;

/**
 * 'go' 명령어에 대응하는 클래스
 * The go command tells the engine to start calculating on the current position (as set up by the position command).
 * Each go command must be eventually responded to with bestmove, once the search is completed or interrupted.
 */
public class UciCommandGo implements UciCommand {
    public void execute(EngineContext context, String args) {
        context.resetStopSignal();

        // 1. 리스너 정의
        SearchListener searchListener = (result -> {
           if (result.moveAnalyses().isEmpty()) {
               context.send("bestmove (none)");
           }
           else{
               context.send("bestmove " + result.moveAnalyses().get(0).move().toString());
           }
        });

        // 2. 비동기 스레드 실행
        new Thread(() -> {
            context.getSearchEngine().search(
                    context,
                    context.getBoard().clone(),
                    searchListener
            );
        }).start();
    }
}
