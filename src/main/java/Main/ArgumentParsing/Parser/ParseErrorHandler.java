package Main.ArgumentParsing.Parser;

import Globals.GlobalErrorCodes;
import picocli.CommandLine;

public class ParseErrorHandler {
    static void handleErrors(CommandLine.ParseResult result) {
        if (!result.errors().isEmpty()) {
//            for (Exception e : result.errors()) {
//                if (e.getMessage().equals("Missing required parameters: '<search string>', '<files/directories>'")){
//                    System.out.println("Missing required parameter: '<search string>'");
//                } else {
//                    System.out.println(e.getMessage());
//                }
//            }
            System.out.println("Usage: mp4grep [options] [search string] [files/directories]"); //| mp4grep [--transcribe | --transcribe-to-files] [options] [files/directories]");
            System.out.println("Try \'mp4grep --help\' for more information");
            System.exit(GlobalErrorCodes.ERROR_EXIT_CODE);
        }
    }
}
