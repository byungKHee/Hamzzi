package hamzzi.uci.command;

import com.github.bhlangonijr.chesslib.Board;
import hamzzi.uci.core.EngineContext;

public class UciCommandPosition implements UciCommand {

    @Override
    public void execute(EngineContext context, String args) {
        // 1. 새로운 보드 인스턴스 생성 (이전 상태 오염 방지)
        Board board = new Board();

        // 2. 초기 위치 설정 (FEN 또는 startpos)
        String fen;
        if (args.contains("startpos")) {
            // chesslib의 Board 기본 생성자는 startpos 상태임
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            board.loadFromFen(fen);
        } else if (args.contains("fen")) {
            fen = extractFen(args);
            board.loadFromFen(fen);
        }

        // 3. moves 이후의 수들을 적용
        if (args.contains("moves ")) {
            String movesPart = args.substring(args.indexOf("moves ") + 6);
            String[] moves = movesPart.split(" ");
            for (String moveStr : moves) {
                if (!moveStr.isEmpty()) {
                    board.doMove(moveStr);
                }
            }
        }

        // 4. 완성된 보드 상태를 컨텍스트에 저장
        context.setBoard(board);
        context.log("Board position updated. Current side: " + board.getSideToMove());
    }

    /**
     * 문자열에서 FEN 부분만 추출하는 헬퍼 메서드
     */
    private String extractFen(String args) {
        int fenStart = args.indexOf("fen ") + 4;
        int movesStart = args.indexOf(" moves");

        if (movesStart != -1) {
            return args.substring(fenStart, movesStart).trim();
        } else {
            return args.substring(fenStart).trim();
        }
    }
}
