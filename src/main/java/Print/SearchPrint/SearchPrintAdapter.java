package Print.SearchPrint;

import Arguments.PrintArguments;
import Print.Printable;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public class SearchPrintAdapter {
    @NonNull private PrintArguments printArguments;

    public SearchPrintAdapter(PrintArguments printArguments) {
        this.printArguments = printArguments;
    }

    public void print(List<Printable> printables) {
        printables.stream().forEach(this::print);
    }

    public void print(Printable printable) {
        SearchPrinter printer = SearchPrinter.builder()
                        .printable(printable)
                        .printArguments(printArguments)
                        .build();

        printer.print();
    }
}
