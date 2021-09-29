package Main.ArgumentParsing.Parser;

import Globals.GlobalErrorCodes;
import picocli.CommandLine;

public class ParseErrorHandler {
    static void handleErrors(CommandLine.ParseResult result) {
        if (!result.errors().isEmpty()) {
            System.out.println("Usage: mp4grep [options] [search string] [files/directories]");
            System.out.println("Try \'mp4grep --help\' for more information");

            for (Exception e : result.errors()) {
                System.out.println(e);
            }

            System.exit(GlobalErrorCodes.ERROR_EXIT_CODE);
        }
    }

    public static void printUsageHelp() {
        System.out.println("This is the help message.");
    }
}
