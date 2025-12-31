package hamzzi.engine.model;

import com.github.bhlangonijr.chesslib.move.Move;

/**
 * 각 보드 상태에 대한 탐색 결과를 저장하는 전위표 (Transposition Table) 클래스.
 * Zobrist 해싱을 사용하여 보드 상태를 고유하게 식별
 */
public class TranspositionTable {
    private final TTEntry table[];
    private int currentAge = 0;

    public TranspositionTable(int sizeMB){
        int entryCount = (sizeMB * 1024 * 1024) / 50; // TTEntry 당 약 50바이트로 가정
        table = new TTEntry[entryCount];
    }

    public void store(long key, Move move, int score, int depth, byte flag) {
        int index = (int) (Math.abs(key) % table.length);
        // Deepest-Wins + Aging 전략 적용
        if (table[index] == null || depth >= table[index].getDepth()) {
            table[index] = new TTEntry(key, move, score, depth, flag, currentAge);
        }
    }

    public TTEntry get(long key){
        int index = (int) (Math.abs(key) % table.length);
        TTEntry entry = table[index];
        if (entry != null && entry.key() == key) {
            return entry;
        }
        return null;
    }

    public void addAge() {
        currentAge ++;
    }
}
