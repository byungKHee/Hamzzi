package hamzzi.engine.model;

import com.github.bhlangonijr.chesslib.move.Move;
import java.util.List;

/**
 * UCI 계층으로 전달되는 최종 탐색 결과 보고서.
 */
public record SearchResult(
    List<MoveAnalyses> moveAnalyses,
    long nodeSearched,  // 총 탐색한 경우의 수
    long timeMs
) {
    public record MoveAnalyses(
      Move move,
      int score,
      int depth,
      List<Move> nextLine // 해당 수 이후 최선의 수순
    ) {}
}
