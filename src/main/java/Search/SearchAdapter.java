package Search;

import Arguments.SearchArguments;
import Print.Printable;

import java.util.LinkedList;
import java.util.List;

public class SearchAdapter {

    private SearchArguments searchArguments;
    public SearchAdapter(SearchArguments searchArguments) {
        this.searchArguments = searchArguments;
    }

    // TODO: multithread this
    public List<Printable> getPrintables(List<Searchable> searchables) {
        List<Printable> result = new LinkedList<Printable>();
        Searcher searcher = new Searcher();

        for (Searchable searchable : searchables) {
            Printable print = searcher.getPrintableSearchResult(searchable, searchArguments.search);
            result.add(print);
        }
        return result;
    }
}
