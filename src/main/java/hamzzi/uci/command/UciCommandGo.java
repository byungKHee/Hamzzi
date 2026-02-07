package hamzzi.uci.command;

import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import hamzzi.engine.model.SearchInfo;
import hamzzi.engine.model.SearchResult;
import hamzzi.engine.searcher.SearchListener;
import hamzzi.uci.core.EngineContext;

import java.util.List;
import java.util.StringJoiner;

/**
 * 'go' 명령어에 대응하는 클래스
 * The go command tells the engine to start calculating on the current position (as set up by the position command).
 * Each go command must be eventually responded to with bestmove, once the search is completed or interrupted.
 */
public class UciCommandGo implements UciCommand {
    private static final long CURRMOVE_INFO_THROTTLE_MS = 100L;

    public void execute(EngineContext context, String args) {
        context.resetStopSignal();
        parseGoParameters(context, args);
        final long[] lastCurrMoveInfoAt = {0L};

        // 1. 리스너 정의
        SearchListener searchListener = new SearchListener() {
            @Override
            public void onSearchInfo(SearchInfo info) {
                if (shouldSkipInfo(info, lastCurrMoveInfoAt)) {
                    return;
                }
                context.send(formatInfo(info));
            }

            @Override
            public void onSearchFinished(SearchResult result) {
                if (result.moveAnalyses().isEmpty() || result.moveAnalyses().get(0).move() == null) {
                    context.send("bestmove (none)");
                } else {
                    context.send("bestmove " + result.moveAnalyses().get(0).move());
                }
            }
        };

        // 2. 비동기 스레드 실행
        new Thread(() -> {
            context.getSearchEngine().search(
                    context,
                    context.getBoard().clone(),
                    searchListener
            );
        }).start();
    }

    private boolean shouldSkipInfo(SearchInfo info, long[] lastCurrMoveInfoAt) {
        // depth 완료 시점(score/pv 포함)은 즉시 전달한다.
        if (info.currMove() == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastCurrMoveInfoAt[0] < CURRMOVE_INFO_THROTTLE_MS) {
            return true;
        }
        lastCurrMoveInfoAt[0] = now;
        return false;
    }

    private void parseGoParameters(EngineContext context, String args) {
        EngineContext.SearchConstraints c = context.getConstraints();
        c.reset();
        boolean infinite = false;

        String[] tokens = args.trim().split("\\s+");
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            switch (token) {
                case "depth" -> {
                    if (i + 1 < tokens.length) c.depth = parseInt(tokens[++i], Integer.MAX_VALUE);
                }
                case "movetime" -> {
                    if (i + 1 < tokens.length) c.movetime = parseLong(tokens[++i], -1);
                }
                case "wtime" -> {
                    if (i + 1 < tokens.length) c.wtime = parseLong(tokens[++i], -1);
                }
                case "btime" -> {
                    if (i + 1 < tokens.length) c.btime = parseLong(tokens[++i], -1);
                }
                case "winc" -> {
                    if (i + 1 < tokens.length) c.winc = parseLong(tokens[++i], 0);
                }
                case "binc" -> {
                    if (i + 1 < tokens.length) c.binc = parseLong(tokens[++i], 0);
                }
                case "movestogo" -> {
                    if (i + 1 < tokens.length) c.movestogo = parseInt(tokens[++i], 0);
                }
                case "infinite" -> {
                    infinite = true;
                    c.movetime = -1;
                    c.allocatedTime = -1;
                }
                default -> {
                }
            }
        }

        c.allocatedTime = infinite ? -1 : calculateAllocatedTime(context, c);
    }

    private long calculateAllocatedTime(EngineContext context, EngineContext.SearchConstraints c) {
        if (c.movetime > 0) {
            return c.movetime;
        }

        Side sideToMove = context.getBoard().getSideToMove();
        long remaining = sideToMove == Side.WHITE ? c.wtime : c.btime;
        long increment = sideToMove == Side.WHITE ? c.winc : c.binc;

        if (remaining <= 0) {
            return -1;
        }

        int movesLeft = c.movestogo > 0 ? c.movestogo : 30;
        long base = remaining / movesLeft;
        long bonus = (long) (increment * 0.8);
        long raw = base + bonus;
        long maxUsable = Math.max(20L, remaining - 20L);
        return Math.max(20L, Math.min(raw, maxUsable));
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String formatInfo(SearchInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("info depth ").append(info.depth())
                .append(" seldepth ").append(info.seldepth())
                .append(" nodes ").append(info.nodes())
                .append(" time ").append(info.timeMs())
                .append(" nps ").append(info.nps())
                .append(" hashfull ").append(info.hashfull());

        if (info.scoreMate() != null) {
            sb.append(" score mate ").append(info.scoreMate());
        } else if (info.scoreCp() != null) {
            sb.append(" score cp ").append(info.scoreCp());
        }

        if (info.currMove() != null) {
            sb.append(" currmove ").append(info.currMove());
            if (info.currMoveNumber() > 0) {
                sb.append(" currmovenumber ").append(info.currMoveNumber());
            }
        }

        List<Move> pv = info.pv();
        if (pv != null && !pv.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" ");
            for (Move move : pv) {
                joiner.add(move.toString());
            }
            sb.append(" pv ").append(joiner);
        }
        return sb.toString();
    }
}
