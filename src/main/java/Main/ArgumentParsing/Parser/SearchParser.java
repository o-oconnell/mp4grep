package Main.ArgumentParsing.Parser;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Globals.GlobalErrorCodes;
import Main.ArgumentParsing.AudioFileFinder;
import Main.Controller;
import Main.SearchController;
import Print.PrintAdapter;
import Search.SearchAdapter;
import Transcribe.TranscriptAdapter;
import Transcribe.VoskAdapter;
import lombok.Builder;
import lombok.NonNull;
import picocli.CommandLine;
@Builder
public class SearchParser implements Parser {
    @NonNull private String[] args;
    private final SearchWorkflowArgs searchWorkflowArgs = new SearchWorkflowArgs();

    public Controller getController() {
        parseSearchArguments(args);
        return SearchController
                .builder()
                .transcriptAdapter(new TranscriptAdapter(getTranscriptArguments()))
                .searchAdapter(new SearchAdapter(getSearchArguments()))
                .printAdapter(new PrintAdapter(getPrintArguments()))
                .build();
    }

    private void parseSearchArguments(String[] args) {
        CommandLine arguments = makeCommandLine(searchWorkflowArgs);
        CommandLine.ParseResult result = arguments.parseArgs(args);
        handleErrors(result);

        if (arguments.isUsageHelpRequested()) {
            ParseErrorHandler.printUsageHelp();
        }
    }

    private CommandLine makeCommandLine(SearchWorkflowArgs args) {
        CommandLine arguments = new CommandLine(args);
        arguments.getCommandSpec().parser().collectErrors(true);
        return arguments;
    }

    private void handleErrors(CommandLine.ParseResult result) {
        if (!result.errors().isEmpty()) {
            System.out.println("Usage: mp4grep [options] [search string] [files/directories]");
            System.out.println("Try \'mp4grep --help\' for more information");

            for (Exception e : result.errors()) {
                System.out.println(e);
            }
            System.exit(GlobalErrorCodes.ERROR_EXIT_CODE);
        }
    }

    private TranscriptArguments getTranscriptArguments() {
        return TranscriptArguments
                .builder()
                .files(AudioFileFinder.getFileList(searchWorkflowArgs.filesAndDirectories))
                .speechToText(getSpeechToText())
                .modelDirectory(searchWorkflowArgs.model)
                .build();
    }

    private VoskAdapter getSpeechToText() {
        return new VoskAdapter();
    }

    private SearchArguments getSearchArguments() {
        return SearchArguments
                .builder()
                .search(searchWorkflowArgs.search)
                .build();
    }

    private PrintArguments getPrintArguments() {
        return PrintArguments
                .builder()
                .wordsAfterMatch(searchWorkflowArgs.wordsToPrintAfterMatch)
                .wordsBeforeMatch(searchWorkflowArgs.wordsToPrintBeforeMatch)
                .search(searchWorkflowArgs.search)
                .build();
    }
}
