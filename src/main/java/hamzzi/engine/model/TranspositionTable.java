package hamzzi.engine.model;

import com.github.bhlangonijr.chesslib.move.Move;

/**
 * 각 보드 상태에 대한 탐색 결과를 저장하는 전위표 (Transposition Table) 클래스.
 * Zobrist 해싱을 사용하여 보드 상태를 고유하게 식별
 */
public class TranspositionTable {
    private final TTEntry table[];
    private final int AGE_MARGIN = 2;
    private int currentAge = 0;

    public TranspositionTable(int sizeMB){
        int entryCount = (sizeMB * 1024 * 1024) / 50; // TTEntry 당 약 50바이트로 가정
        table = new TTEntry[entryCount];
    }

    public void store(long key, Move move, int score, int depth, byte flag) {
        int index = getIndex(key);
        TTEntry existingEntry = table[index];

        // 1. 빈 슬롯이면 즉시 저장
        if (existingEntry == null) {
            table[index] = new TTEntry(key, move, score, depth, flag, currentAge);
            return;
        }

        // 2. 같은 보드인 경우
        // - depth 기준으로 교체
        if (existingEntry.key() == key) {
            if (depth >= existingEntry.depth()) {
                table[index] = new TTEntry(key, move, score, depth, flag, currentAge);
            }
            return;
        }

        // 3. 다른 보드인 경우
        // - AGE_MARGIN 이상의 유예 기간이 지난 경우 교체합니다.
        // - 혹은 유예 기간 내에 있더라도 새 데이터의 깊이가 더 깊다면 교체합니다.
        boolean isOld = (this.currentAge - existingEntry.age()) >= AGE_MARGIN;
        boolean isDeeper = depth >= existingEntry.depth();

        if (isOld || isDeeper) {
            table[index] = new TTEntry(key, move, score, depth, flag, currentAge);
        }
    }

    public TTEntry get(long key) {
        int index = getIndex(key);
        TTEntry entry = table[index];

        if (entry != null && entry.key() == key) {
            return entry;
        }
        return null;
    }

    public void addAge() {
        currentAge ++;
    }

    private int getIndex(long key) {
        return (int) ((key & Long.MAX_VALUE) % table.length);
    }
}
