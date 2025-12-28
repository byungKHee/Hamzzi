package hamzzi.engine.searcher;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.model.SearchResult;
import hamzzi.uci.core.EngineContext;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSearchEngine implements SearchEngine {

    private final Random random = new Random();

    @Override
    public void search(EngineContext context, Board board, SearchListener listener) {
        context.log("Random Search Engine started...");

        // 1. 합법적인 수 확보
        List<Move> legalMoves = board.legalMoves();
        if (legalMoves.isEmpty()) {
            listener.onSearchFinished(new SearchResult(Collections.emptyList(), 0, 0));
            return;
        }

        // 2. 잠깐의 delay
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++){
            if (context.isStopped()) break;
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }

        // 3. 무작위 수 반환
        Move bestMove = legalMoves.get(random.nextInt(legalMoves.size()));
        SearchResult.MoveAnalysis analasis = new SearchResult.MoveAnalysis(
                bestMove,
                0,
                1,
                Collections.emptyList()
        );

        SearchResult result = new SearchResult(
                List.of(analasis),
                legalMoves.size(),
                System.currentTimeMillis() - start
        );

        // 4. 리스너에게 전달
        listener.onSearchFinished(result);
    }
}
