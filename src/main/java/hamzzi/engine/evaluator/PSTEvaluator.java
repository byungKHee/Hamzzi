package hamzzi.engine.evaluator;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;

import java.util.HashMap;
import java.util.Map;

public class PSTEvaluator implements Evaluator {

    // checkmate and draw constants
    private static final int MATED = -9999999;
    private static final int DRAW = 0;

    // piece value constants
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    // Piece-Square Tables (PST)
    private static final Map<PieceType, int[]> PST = new HashMap<>();
    static {
        PST.put(PieceType.PAWN, new int[]{
                0,   0,   0,   0,   0,   0,   0,   0,
                78,  83,  86,  73, 102,  82,  85,  90,
                7,  29,  21,  44,  40,  31,  44,   7,
                -17,  16,  -2,  15,  14,   0,  15, -13,
                -26,   3,  10,   9,   6,   1,   0, -23,
                -22,   9,   5, -11, -10,  -2,   3, -19,
                -31,   8,  -7, -37, -36, -14,   3, -31,
                0,   0,   0,   0,   0,   0,   0,   0
        });
        PST.put(PieceType.KNIGHT, new int[]{
                -66, -53, -75, -75, -10, -55, -58, -70,
                -3,  -6, 100, -36,   4,  62,  -4, -14,
                10,  67,   1,  74,  73,  27,  62,  -2,
                24,  24,  45,  37,  33,  41,  25,  17,
                -1,   5,  31,  21,  22,  35,   2,   0,
                -18,  10,  13,  22,  18,  15,  11, -14,
                -23, -15,   2,   0,   2,   0, -23, -20,
                -74, -23, -26, -24, -19, -35, -22, -69
        });
        PST.put(PieceType.BISHOP, new int[]{
                -59, -78, -82, -76, -23,-107, -37, -50,
                -11,  20,  35, -42, -39,  31,   2, -22,
                -9,  39, -32,  41,  52, -10,  28, -14,
                25,  17,  20,  34,  26,  25,  15,  10,
                13,  10,  17,  23,  17,  16,   0,   7,
                14,  25,  24,  15,   8,  25,  20,  15,
                19,  20,  11,   6,   7,   6,  20,  16,
                -7,   2, -15, -12, -14, -15, -10, -10
        });
        PST.put(PieceType.ROOK, new int[]{
                35,  29,  33,   4,  37,  33,  56,  50,
                55,  29,  56,  67,  55,  62,  34,  60,
                19,  35,  28,  33,  45,  27,  25,  15,
                0,   5,  16,  13,  18,  -4,  -9,  -6,
                -28, -35, -16, -21, -13, -29, -46, -30,
                -42, -28, -42, -25, -25, -35, -26, -46,
                -53, -38, -31, -26, -29, -43, -44, -53,
                -30, -24, -18,   5,  -2, -18, -31, -32
        });
        PST.put(PieceType.QUEEN, new int[]{
                6,   1,  -8,-104,  69,  24,  88,  26,
                14,  32,  60, -10,  20,  76,  57,  24,
                -2,  43,  32,  60,  72,  63,  43,   2,
                1, -16,  22,  17,  25,  20, -13,  -6,
                -14, -15,  -2,  -5,  -1, -10, -20, -22,
                -30,  -6, -13, -11, -16, -11, -16, -27,
                -36, -18,   0, -19, -15, -15, -21, -38,
                -39, -30, -31, -13, -31, -36, -34, -42
        });
        PST.put(PieceType.KING, new int[]{
                4,  54,  47, -99, -99,  60,  83, -62,
                -32,  10,  55,  56,  56,  55,  10,   3,
                -62,  12, -57,  44, -67,  28,  37, -31,
                -55,  50,  11,  -4, -19,  13,   0, -49,
                -55, -43, -52, -28, -51, -47,  -8, -50,
                -47, -42, -43, -79, -64, -32, -29, -32,
                -4,   3, -14, -50, -57, -18,  13,   4,
                17,  30,  -3, -14,   6,  -1,  40,  18
        });
    }
    @Override
    public int evaluate(Board board) {
        if (board.isMated()) return MATED;
        if (board.isDraw()) return DRAW;

        int whiteScore = calculateSideScore(board, Side.WHITE);
        int blackScore = calculateSideScore(board, Side.BLACK);

        int totalScore = whiteScore - blackScore;

        return board.getSideToMove() == Side.WHITE ? totalScore : -totalScore;
    }

    private int calculateSideScore(Board board, Side side) {
        int score = 0;

        for (PieceType type : PieceType.values()) {
            if (type == PieceType.NONE) continue;

            // 기물 가치 합산
            Piece piece = Piece.make(side, type);
            long bitboard = board.getBitboard(piece);
            int count = Long.bitCount(bitboard);
            score += count * getMaterialValue(type);

            // PST 점수 합산
            int[] table = PST.get(type);
            while (bitboard != 0L) {
                int squareIndex = Long.numberOfTrailingZeros(bitboard);
                score += getPstValue(table, squareIndex, side);
                bitboard &= (bitboard - 1);
            }
        }
        return score;
    }

    private int getMaterialValue(PieceType type) {
        return switch (type) {
            case PAWN -> PAWN_VALUE;
            case KNIGHT -> KNIGHT_VALUE;
            case BISHOP -> BISHOP_VALUE;
            case ROOK -> ROOK_VALUE;
            case QUEEN -> QUEEN_VALUE;
            case KING -> KING_VALUE;
            default -> 0;
        };
    }

    private int getPstValue(int[] table, int squareIndex, Side side) {
        int r = squareIndex / 8; // 0-7 (Rank 1-8)
        int f = squareIndex % 8; // 0-7 (File A-H)

        if (side == Side.WHITE) {
            // chesslib은 0이 a1 기준이므로 변환: (7 - r) * 8 + f
            return table[(7 - r) * 8 + f];
        } else {
            // 흑은 판을 뒤집어서 계산
            return table[r * 8 + f];
        }
    }


}
