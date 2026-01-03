package hamzzi.uci.core;

import com.github.bhlangonijr.chesslib.Board;
import hamzzi.engine.model.TranspositionTable;
import hamzzi.engine.searcher.AlphaBetaSearchEngine;
import hamzzi.engine.searcher.MinMaxSearchEngine;
import hamzzi.engine.searcher.SearchEngine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 엔진의 모든 상태와 설정, 공유 자원을 관리하는 컨텍스트 클래스.
 * 모든 UCI 명령어 객체는 이 객체를 공유하여 엔진 상태를 변경하거나 조회합니다.
 */
public class EngineContext {
    // 1. 현재 체스판 상태 (원본)
    private Board board = new Board();

    // 2. 치환표 (Transposition Table): 계산 결과 공유용 맵
    // Key: Board의 Zobrist Key(Long), Value: 탐색 결과(Score, BestMove 등)
    // 멀티스레드 환경이므로 ConcurrentHashMap 사용
    private final TranspositionTable tt = new TranspositionTable(256);

    // 3. 제어 신호: 검색 스레드들이 주기적으로 확인하여 즉시 중단하게 함
    private volatile boolean stopSignal = false;

    // 4. 사용할 엔진 종류
    private SearchEngine searchEngine = new AlphaBetaSearchEngine(new TranspositionTable(128));

    // 5. 엔진 설정값 (UCI Options)
    private final Map<String, String> options = new ConcurrentHashMap<>();

    // 6. 검색 제한 조건 (Time Control & Constraints)
    private final SearchConstraints constraints = new SearchConstraints();

    public EngineContext() {
        // 기본 옵션 초기화
        options.put("Hash", "16");
        options.put("Threads", "1");
    }

    // --- Board 관리 ---
    public synchronized Board getBoard() {
        return board;
    }

    public synchronized void setBoard(Board board) {
        this.board = board;
    }

    // --- 검색 제어 (Stop Signal) ---
    public void stopSearch() {
        this.stopSignal = true;
    }

    public void resetStopSignal() {
        this.stopSignal = false;
    }

    public boolean isStopped() {
        return stopSignal;
    }

    // --- 엔진 관리 ---
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    // --- 치환표(TT) 관리 ---
    public TranspositionTable getTranspositionTable() {
        return tt;
    }

    // --- 옵션 관리 ---
    public void setOption(String name, String value) {
        options.put(name, value);
    }

    public String getOption(String name) {
        return options.get(name);
    }

    // --- 검색 제한 조건 (Inner Class) ---
    public SearchConstraints getConstraints() {
        return constraints;
    }

    public static class SearchConstraints {
        // GUI가 보내주는 원본 데이터
        public long wtime = -1;
        public long btime = -1;
        public long winc = 0;
        public long binc = 0;
        public int movestogo = 0;
        public int depth = Integer.MAX_VALUE;
        public long movetime = -1; // Hard limit

        // 엔진이 계산한 이번 수의 할당 시간 (Soft limit)
        public long allocatedTime = -1;

        public void reset() {
            wtime = btime = movetime = allocatedTime = -1;
            winc = binc = 0;
            movestogo = 0;
            depth = Integer.MAX_VALUE;
        }
    }

    // --- GUI 응답 유틸리티 ---
    public void send(String message) {
        System.out.println(message);
        System.out.flush();
    }

    public void log(String info) {
        send("info string [HAMZZI] " + info);
    }
}