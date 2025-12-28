package hamzzi.engine.model;

import com.github.bhlangonijr.chesslib.move.Move;

/**
 * 치환표(Transposition Table)에 저장되는 캐시 레코드
 * 각 포지션에 대한 최선 수 하나를 저장함
 */
public record SearchEntry(
        int score,
        int depth,
        Move bestMove
) {}
