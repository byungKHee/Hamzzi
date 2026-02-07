package hamzzi.engine.searcher;

import hamzzi.engine.model.SearchInfo;
import hamzzi.engine.model.SearchResult;

public interface SearchListener {
    default void onSearchInfo(SearchInfo info) {}

    void onSearchFinished(SearchResult result);
}
