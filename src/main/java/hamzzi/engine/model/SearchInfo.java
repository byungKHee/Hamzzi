package hamzzi.engine.model;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.List;

/**
 * 탐색 중간 상태를 UCI info 형식으로 전달하기 위한 데이터 객체.
 */
public record SearchInfo(
        int depth,
        int seldepth,
        Integer scoreCp,
        Integer scoreMate,
        long nodes,
        long timeMs,
        long nps,
        List<Move> pv,
        Move currMove,
        int currMoveNumber,
        int hashfull
) {
}
