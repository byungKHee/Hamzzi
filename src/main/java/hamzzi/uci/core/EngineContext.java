package hamzzi.uci.core;

import com.github.bhlangonijr.chesslib.Board;

public class EngineContext {
    private Board board = new Board();
    private boolean isQuitting = false;

    // Getter, Setter
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    public void quit() { this.isQuitting = true; }
    public boolean isQuitting() { return isQuitting; }

    public void send(String msg) {
        System.out.println(msg);
        System.out.flush();
    }
}