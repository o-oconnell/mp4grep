package Main;

import Arguments.GrepperArguments;
import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Print.PrintAdapter;
import Print.Printable;
import Search.SearchAdapter;
import Search.Searchable;
import Transcription.TranscriptionAdapter;

import java.util.List;

public class Grepper {

    GrepperArguments arguments;
    TranscriptArguments transcriptArguments;
    SearchArguments searchArguments;
    PrintArguments printArguments;

    public Grepper(GrepperArguments arguments) {
        this.arguments = arguments;
        this.transcriptArguments = arguments.transcriptArguments;
        this.searchArguments = arguments.searchArguments;
        this.printArguments = arguments.printArguments;
    }

    public void execute() {
        TranscriptionAdapter sttAdapter = new TranscriptionAdapter(transcriptArguments);
        SearchAdapter searchAdapter = new SearchAdapter(searchArguments);
        PrintAdapter printAdapter = new PrintAdapter(printArguments);

        List<Searchable> searchables = sttAdapter.getSearchables();
        List<Printable> printables = searchAdapter.getPrintables(searchables);
        printAdapter.print(printables);
    }
}
