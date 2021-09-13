package Print;

import Arguments.PrintArguments;
import lombok.Builder;

import java.util.List;

@Builder
public class PrintAdapter {
    PrintArguments printArguments;

    public PrintAdapter(PrintArguments printArguments) {
        this.printArguments = printArguments;
    }

    public void print(List<Printable> printables) {
        Printer printer = new Printer(printArguments);

        for (Printable printable : printables) {
            printer.print(printable);
        }
    }
}
