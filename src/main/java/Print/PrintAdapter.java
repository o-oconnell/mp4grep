package Print;

import Arguments.PrintArguments;

import java.util.List;

public class PrintAdapter {

    PrintArguments args;
    public PrintAdapter(PrintArguments args) {
        this.args = args;
    }

    public void print(List<Printable> printables) {
        Printer printer = new Printer(args);
        for (Printable printable : printables) {
            printer.print(printable);
        }
    }
}
