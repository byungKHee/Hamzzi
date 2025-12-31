package hamzzi.engine.model;
import com.github.bhlangonijr.chesslib.move.Move;

/**
 * 전위표 (Transposition Table) 항목을 나타내는 불변 데이터 클래스.
 * 각 항목은 특정 보드 상태에 대한 탐색 결과를 저장
 */
public record TTEntry(
        long key,      // Zobrist Key
        Move bestMove, // 최선수
        int score,     // 점수
        int depth,     // 탐색 깊이
        byte flag,     // EXACT, ALPHA, BETA
        int age        // 생성 세대
) {
    // 1. EXACT: 점수가 정확한 값 (Alpha < Score < Beta)
    // 2. ALPHA (Upper Bound): 모든 수가 Alpha보다 낮음. 실제 점수는 이 값보다 작거나 같음.
    // 3. BETA (Lower Bound): 어떤 수가 Beta 컷오프를 발생시킴. 실제 점수는 이 값보다 크거나 같음.
    public static final byte EXACT = 0;
    public static final byte ALPHA = 1;
    public static final byte BETA = 2;

    public int getDepth() {
        return depth;
    }
}