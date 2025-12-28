package hamzzi.engine.searcher;

import com.github.bhlangonijr.chesslib.Board;
import hamzzi.uci.core.EngineContext;

public interface SearchEngine {
    void search(EngineContext context, Board board, SearchListener listener);
}
