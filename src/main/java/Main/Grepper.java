package Main;

import Print.PrintAdapter;
import Print.Printable;
import Search.SearchAdapter;
import Search.Searchable;
import Transcribe.TranscriptAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class Grepper {
    TranscriptAdapter transcriptionAdapter;
    SearchAdapter searchAdapter;
    PrintAdapter printAdapter;

    public void execute() {
        List<Searchable> searchables = transcriptionAdapter.getSearchables();
        List<Printable> printables = searchAdapter.getPrintables(searchables);
        printAdapter.print(printables);
    }
}
