package hamzzi.engine.searcher;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.evaluator.Evaluator;
import hamzzi.engine.evaluator.SimpleEvaluator;
import hamzzi.engine.model.SearchResult;
import hamzzi.engine.model.TTEntry;
import hamzzi.engine.model.TranspositionTable;
import hamzzi.uci.core.EngineContext;

import java.util.Collections;
import java.util.List;

public class AlphaBetaSearchEngine implements SearchEngine {
    private final Evaluator evaluator = new SimpleEvaluator();
    private final TranspositionTable tt;

    private static final int INF = 10000000;
    private static final int MATE_SCORE = 1000000;
    private final int MAX_DEPTH = 6;

    public AlphaBetaSearchEngine(TranspositionTable tt) {
        this.tt = tt;
    }

    @Override
    public void search(EngineContext context, Board board, SearchListener listener) {
        Move finalBestMove = null;
        int finalBestScore = -INF;

        // 1. Iterative Deepening
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            int alpha = -INF;
            int beta = INF;
            Move currentDepthBestMove = null;

            List<Move> moves = board.legalMoves();

            // 2. Root Move Ordering
            Move ttMove = getTTMove(board);
            sortTTMoveToFront(moves, ttMove);

            for (Move move : moves) {
                board.doMove(move);
                int score = -alphaBeta(board, -beta, -alpha, depth - 1, 1);
                board.undoMove();

                if (score > alpha) {
                    alpha = score;
                    currentDepthBestMove = move;
                }
            }

            finalBestMove = currentDepthBestMove;
            finalBestScore = alpha;
        }
        SearchResult.MoveAnalysis analysis = new SearchResult.MoveAnalysis(
                finalBestMove,
                finalBestScore,
                0,
                Collections.emptyList()
        );
        listener.onSearchFinished(new SearchResult(List.of(analysis), 0, 0));
    }

    private int alphaBeta(Board board, int alpha, int beta, int depth, int ply) {
        long key = board.getZobristKey();

        // 1. TT 조회
        TTEntry entry = tt.get(key);
        if (entry != null && entry.depth() >= depth) {
            if (entry.flag() == TTEntry.EXACT) return entry.score();
            if (entry.flag() == TTEntry.ALPHA && entry.score() <= alpha) return alpha;
            if (entry.flag() == TTEntry.BETA && entry.score() >= beta) return beta;
        }

        if (board.isMated()) return -MATE_SCORE + ply;
        if (board.isDraw()) return 0;
        if (depth <= 0) return evaluator.evaluate(board);

        int bestScore = -INF;
        Move bestMove = null;
        int originalAlpha = alpha;

        // 2. Simple Move Ordering
        List<Move> moves = board.legalMoves();
        Move ttMove = (entry != null) ? entry.bestMove() : null;
        sortTTMoveToFront(moves, ttMove);

        for (Move move : moves) {
            board.doMove(move);
            int score = -alphaBeta(board, -beta, -alpha, depth - 1, ply + 1);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // Beta 컷오프
        }

        // 3. TT 저장 & 업데이트
        byte flag = TTEntry.EXACT;
        if (bestScore <= originalAlpha) flag = TTEntry.ALPHA;
        else if (bestScore >= beta) flag = TTEntry.BETA;

        tt.store(key, bestMove, bestScore, depth, flag);
        return bestScore;
    }

    private void sortTTMoveToFront(List<Move> moves, Move ttMove) {
        if (ttMove == null) return;
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i).equals(ttMove)) {
                Collections.swap(moves, 0, i);
                break;
            }
        }
    }

    private Move getTTMove(Board board) {
        TTEntry entry = tt.get(board.getZobristKey());
        return (entry != null) ? entry.bestMove() : null;
    }
}