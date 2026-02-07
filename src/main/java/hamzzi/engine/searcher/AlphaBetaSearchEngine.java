package hamzzi.engine.searcher;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.evaluator.Evaluator;
import hamzzi.engine.evaluator.SimpleEvaluator;
import hamzzi.engine.model.SearchInfo;
import hamzzi.engine.model.SearchResult;
import hamzzi.engine.model.TTEntry;
import hamzzi.engine.model.TranspositionTable;
import hamzzi.engine.presenter.PVExtractor;
import hamzzi.uci.core.EngineContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlphaBetaSearchEngine implements SearchEngine {
    private final Evaluator evaluator = new SimpleEvaluator();

    private static final int INF = 10_000_000;
    private static final int MATE_SCORE = 1_000_000;
    private static final int DEFAULT_MAX_DEPTH = 64;
    private static final int MAX_PLY = 128;

    private final Move[][] killerMoves = new Move[MAX_PLY][2];
    private final int[][] historyHeuristic = new int[64][64];

    private long nodes;
    private long startTimeMs;
    private long hardStopTimeMs;
    private int currentSelDepth;

    public AlphaBetaSearchEngine() {
    }

    @Override
    public void search(EngineContext context, Board board, SearchListener listener) {
        TranspositionTable tt = context.getTranspositionTable();
        Move finalBestMove = null;
        int finalBestScore = -INF;
        int completedDepth = 0;

        nodes = 0;
        startTimeMs = System.currentTimeMillis();
        currentSelDepth = 0;

        EngineContext.SearchConstraints constraints = context.getConstraints();
        hardStopTimeMs = constraints.allocatedTime > 0 ? startTimeMs + constraints.allocatedTime : -1;
        int maxDepth = constraints.depth == Integer.MAX_VALUE ? DEFAULT_MAX_DEPTH : constraints.depth;

        for (int depth = 1; depth <= maxDepth; depth++) {
            if (shouldStop(context)) break;

            int alpha = -INF;
            int beta = INF;
            Move currentDepthBestMove = null;

            List<Move> moves = board.legalMoves();
            if (moves.isEmpty()) break;

            Move ttMove = getTTMove(board, tt);
            sortRootMoves(board, moves, ttMove, finalBestMove);

            for (Move move : moves) {
                if (shouldStop(context)) break;

                board.doMove(move);
                int score = -alphaBeta(context, board, -beta, -alpha, depth - 1, 1, tt);
                board.undoMove();

                if (score > alpha) {
                    alpha = score;
                    currentDepthBestMove = move;
                }
            }

            if (currentDepthBestMove == null) break;

            finalBestMove = currentDepthBestMove;
            finalBestScore = alpha;
            completedDepth = depth;

            long elapsed = System.currentTimeMillis() - startTimeMs;
            long nps = elapsed > 0 ? (nodes * 1000L) / elapsed : nodes;
            listener.onSearchInfo(new SearchInfo(
                    depth,
                    currentSelDepth,
                    finalBestScore,
                    nodes,
                    elapsed,
                    nps,
                    PVExtractor.extract(board, depth, tt)
            ));

            if (Math.abs(finalBestScore) >= MATE_SCORE - 200) {
                break;
            }
        }

        SearchResult.MoveAnalysis analysis = new SearchResult.MoveAnalysis(
                finalBestMove,
                finalBestScore,
                completedDepth,
                finalBestMove == null ? Collections.emptyList() : PVExtractor.extract(board, completedDepth, tt)
        );

        listener.onSearchFinished(new SearchResult(
                finalBestMove == null ? Collections.emptyList() : List.of(analysis),
                nodes,
                System.currentTimeMillis() - startTimeMs
        ));
    }

    private int alphaBeta(
            EngineContext context,
            Board board,
            int alpha,
            int beta,
            int depth,
            int ply,
            TranspositionTable tt
    ) {
        if (shouldStop(context)) return evaluator.evaluate(board);

        nodes++;
        currentSelDepth = Math.max(currentSelDepth, ply);

        long key = board.getZobristKey();

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

        List<Move> moves = board.legalMoves();
        if (moves.isEmpty()) return evaluator.evaluate(board);

        Move ttMove = entry != null ? entry.bestMove() : null;
        sortMoves(board, moves, ttMove, ply);

        for (Move move : moves) {
            if (shouldStop(context)) break;

            boolean isCapture = isCapture(board, move);
            board.doMove(move);
            int score = -alphaBeta(context, board, -beta, -alpha, depth - 1, ply + 1, tt);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            if (score > alpha) {
                alpha = score;
            }

            if (alpha >= beta) {
                if (!isCapture) {
                    updateKillerMoves(move, ply);
                    updateHistory(move, depth);
                }
                break;
            }
        }

        if (bestMove == null) return evaluator.evaluate(board);

        byte flag = TTEntry.EXACT;
        if (bestScore <= originalAlpha) flag = TTEntry.ALPHA;
        else if (bestScore >= beta) flag = TTEntry.BETA;

        tt.store(key, bestMove, bestScore, depth, flag);
        return bestScore;
    }

    private boolean shouldStop(EngineContext context) {
        if (context.isStopped()) {
            return true;
        }
        return hardStopTimeMs > 0 && System.currentTimeMillis() >= hardStopTimeMs;
    }

    private void sortRootMoves(Board board, List<Move> moves, Move ttMove, Move previousBestMove) {
        moves.sort(Comparator.comparingInt((Move m) -> moveOrderScore(board, m, 0, ttMove, previousBestMove)).reversed());
    }

    private void sortMoves(Board board, List<Move> moves, Move ttMove, int ply) {
        moves.sort(Comparator.comparingInt((Move m) -> moveOrderScore(board, m, ply, ttMove, null)).reversed());
    }

    private int moveOrderScore(Board board, Move move, int ply, Move ttMove, Move previousBestMove) {
        if (sameMove(move, ttMove)) return 2_000_000;
        if (sameMove(move, previousBestMove)) return 1_900_000;

        if (isCapture(board, move)) {
            return 1_000_000 + mvvLvaScore(board, move);
        }

        int score = 0;
        if (ply < MAX_PLY) {
            if (sameMove(move, killerMoves[ply][0])) score += 900_000;
            else if (sameMove(move, killerMoves[ply][1])) score += 850_000;
        }

        score += historyHeuristic[move.getFrom().ordinal()][move.getTo().ordinal()];
        return score;
    }

    private int mvvLvaScore(Board board, Move move) {
        Piece victim = board.getPiece(move.getTo());
        Piece attacker = board.getPiece(move.getFrom());

        int victimValue = pieceValue(victim);
        int attackerValue = pieceValue(attacker);
        return victimValue * 10 - attackerValue;
    }

    private int pieceValue(Piece piece) {
        return switch (piece.getPieceType()) {
            case PAWN -> 100;
            case KNIGHT -> 320;
            case BISHOP -> 330;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 20_000;
            default -> 0;
        };
    }

    private boolean isCapture(Board board, Move move) {
        return board.getPiece(move.getTo()) != Piece.NONE;
    }

    private void updateKillerMoves(Move move, int ply) {
        if (ply >= MAX_PLY || sameMove(move, killerMoves[ply][0])) return;
        killerMoves[ply][1] = killerMoves[ply][0];
        killerMoves[ply][0] = move;
    }

    private void updateHistory(Move move, int depth) {
        int from = move.getFrom().ordinal();
        int to = move.getTo().ordinal();
        int bonus = depth * depth;

        int updated = historyHeuristic[from][to] + bonus;
        historyHeuristic[from][to] = Math.min(updated, 1_000_000);
    }

    private boolean sameMove(Move a, Move b) {
        return a != null && a.equals(b);
    }

    private Move getTTMove(Board board, TranspositionTable tt) {
        TTEntry entry = tt.get(board.getZobristKey());
        return entry != null ? entry.bestMove() : null;
    }
}
