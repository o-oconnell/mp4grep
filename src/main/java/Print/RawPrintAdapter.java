package Print;

import Arguments.RawPrintArguments;
import Search.Searchable;

import java.util.List;

public class RawPrintAdapter {
    private RawPrintArguments arguments;

    public RawPrintAdapter(RawPrintArguments arguments) {
        this.arguments = arguments;
    }

    public void print(List<Searchable> searchables) {
        RawPrinter rawPrinter = new RawPrinter(arguments);
        searchables.stream()
                .forEach(rawPrinter::print);
    }
}
