package hamzzi.engine.searcher;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.evaluator.Evaluator;
import hamzzi.engine.evaluator.SimpleEvaluator;
import hamzzi.engine.model.SearchResult;
import hamzzi.uci.core.EngineContext;

import java.util.Collections;
import java.util.List;

public class MinMaxSearchEngine implements SearchEngine {

    private final Evaluator evaluator = new SimpleEvaluator();

    @Override
    public void search(EngineContext context, Board board, SearchListener listener){
        int bestScore = -999999;
        Move bestMove = null;

        int depth = 4;
        for (Move move : board.legalMoves()) {
            board.doMove(move);
            int score = -minimax(board, depth - 1);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        SearchResult.MoveAnalysis analysis = new SearchResult.MoveAnalysis(
                bestMove,
                bestScore,
                depth,
                Collections.emptyList()
        );
        listener.onSearchFinished(new SearchResult(List.of(analysis), 0, 0));
    }

    private int minimax(Board board, int depth) {
        if (depth == 0) return evaluator.evaluate(board);

        int maxScore = -999999;
        for (Move move : board.legalMoves()) {
            board.doMove(move);
            int score = -minimax(board, depth - 1);
            board.undoMove();

            if (score > maxScore) maxScore = score;
        }
        return maxScore;
    }

}
