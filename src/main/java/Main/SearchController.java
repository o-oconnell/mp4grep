package Main;

import Print.SearchPrint.SearchPrintAdapter;
import Print.Printable;
import Search.SearchAdapter;
import Search.Searchable;
import Transcribe.TranscriptAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class SearchController implements Controller {
    TranscriptAdapter transcriptAdapter;
    SearchAdapter searchAdapter;
    SearchPrintAdapter printAdapter;

    @Override
    public void execute() {
        List<Searchable> searchables = transcriptAdapter.getSearchables();
        List<Printable> printables = searchAdapter.getPrintables(searchables);
        printAdapter.print(printables);
    }
}
