package Search;

import Arguments.SearchArguments;
import Print.Printable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchAdapter {

    private SearchArguments searchArguments;
    public SearchAdapter(SearchArguments searchArguments) {
        this.searchArguments = searchArguments;
    }

    public List<Printable> getPrintables(List<Searchable> searchables) {
        return searchables.parallelStream()
                .map(this::search)
                .collect(Collectors.toList());
    }

    private Printable search(Searchable searchable) {
        Searcher searcher = new Searcher();
        return searcher.getPrintableSearchResult(searchable, searchArguments.search);
    }
}
