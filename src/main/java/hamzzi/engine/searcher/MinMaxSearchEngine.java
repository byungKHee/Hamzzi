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

    // 점수 관련 상수
    private static final int MAX_DEPTH = 4;
    private static final int MATE_SCORE = 1000000;
    private static final int DRAW_SCORE = 0;

    @Override
    public void search(EngineContext context, Board board, SearchListener listener){
        int bestScore = -Integer.MAX_VALUE;
        Move bestMove = null;

        for (Move move : board.legalMoves()) {
            board.doMove(move);
            int score = -minimax(board, 1);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        SearchResult.MoveAnalysis analysis = new SearchResult.MoveAnalysis(
                bestMove,
                bestScore,
                0,
                Collections.emptyList()
        );
        listener.onSearchFinished(new SearchResult(List.of(analysis), 0, 0));
    }

    private int minimax(Board board, int currentDepth) {
        // 1. 책임 분리: 게임 종료 상태(메이트, 무승부)에 대한 점수 산출
        if (board.isMated() || board.isDraw()) {
            if (board.isMated()) {
                return -MATE_SCORE + currentDepth; // 메이트에 가까울수록 점수가 높음
            }
            return DRAW_SCORE;
        }

        // 2. 기저 조건: 설정한 최대 깊이에 도달하면 정적 평가 수행
        if (currentDepth >= MAX_DEPTH) {
            return evaluator.evaluate(board);
        }

        // 3. 재귀 탐색: 자식 노드 중 최대값 선택
        int maxScore = -Integer.MAX_VALUE;
        for (Move move : board.legalMoves()) {
            board.doMove(move);
            int score = -minimax(board, currentDepth + 1);
            board.undoMove();

            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

}
