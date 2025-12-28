package hamzzi.engine.searcher;

import hamzzi.engine.model.SearchResult;

public interface SearchListener {
    void onSearchFinished(SearchResult result);
}
