package hamzzi.engine.evaluator;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;

public class SimpleEvaluator implements Evaluator {

    private static final int MATED = -9999999;
    private static final int DRAW = 0;

    @Override
    public int evaluate(Board board){

        // 체크메이트
        if (board.isMated()){
            return MATED;
        }
        // 무승부
        if (board.isDraw()){
            return DRAW;
        }

        // 현재는 단순 기물 점수만으로 계산
        int score = 0;
        score += countMaterial(board, Side.WHITE) - countMaterial(board, Side.BLACK);

        return board.getSideToMove() == Side.WHITE ? score : -score;
    }

    private int countMaterial(Board board, Side side) {
        int total = 0;
        total += Long.bitCount(board.getBitboard(Piece.make(side, PieceType.PAWN))) * 100;
        total += Long.bitCount(board.getBitboard(Piece.make(side, PieceType.KNIGHT))) * 320;
        total += Long.bitCount(board.getBitboard(Piece.make(side, PieceType.BISHOP))) * 330;
        total += Long.bitCount(board.getBitboard(Piece.make(side, PieceType.ROOK))) * 500;
        total += Long.bitCount(board.getBitboard(Piece.make(side, PieceType.QUEEN))) * 900;
        return total;
    }
}
