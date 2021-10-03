package Print.TranscribePrint;

import Arguments.RawPrintArguments;
import Search.Searchable;

import java.util.List;

public class TranscribePrintAdapter {
    private RawPrintArguments arguments;

    public TranscribePrintAdapter(RawPrintArguments arguments) {
        this.arguments = arguments;
    }

    public void print(List<Searchable> searchables) {
        TranscribePrinter rawPrinter = new TranscribePrinter(arguments);
        searchables.stream()
                .forEach(rawPrinter::print);
    }
}
