package hamzzi.engine.presenter;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.model.TTEntry;
import hamzzi.engine.model.TranspositionTable;

import java.util.ArrayList;
import java.util.List;

/**
 * TT를 바탕으로 현채 보드에서의 최선의 수(PV)를 추출하는 역할을 담당하는 클래스
 */
public class PVExtractor {
    public static List<Move> extract(Board board, int maxDepth, TranspositionTable TT) {
        List<Move> pv = new ArrayList<>();

        Board tempBoard = board.clone();

        for (int i = 0; i < maxDepth; i++) {
            // 1. TT에 현재 보드 상태에 대한 항목 조회
            long key = tempBoard.getZobristKey();
            TTEntry entry = TT.get(key);

            // 2. TT에 해당 보드 상태가 없으면 maxDepth 이전에 종료
            if (entry == null) {
                break;
            }

            Move bestMove = entry.bestMove();

            // 3. 최선의 수가 합법적인 수인지 확인
            if (!tempBoard.legalMoves().contains(bestMove)) {
                break;
            }

            // 4. 최선의 수를 PV에 추가하고 보드에 적용
            pv.add(bestMove);
            tempBoard.doMove(bestMove);

            // 5. 체크메이트나 스테일메이트인 경우 종료
            if (tempBoard.isMated() || tempBoard.isDraw()) {
                break;
            }
        }

        return pv;
    }
}
