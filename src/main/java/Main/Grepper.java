package Main;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Print.PrintAdapter;
import Print.Printable;
import Search.SearchAdapter;
import Search.Searchable;
import Transcription.TranscriptionAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class Grepper {
    TranscriptionAdapter transcriptionAdapter;
    SearchAdapter searchAdapter;
    PrintAdapter printAdapter;

    public void execute() {
        List<Searchable> searchables = transcriptionAdapter.getSearchables();
        List<Printable> printables = searchAdapter.getPrintables(searchables);
        printAdapter.print(printables);
    }
}
