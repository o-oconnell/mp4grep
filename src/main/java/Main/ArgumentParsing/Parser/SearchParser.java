package Main.ArgumentParsing.Parser;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Main.ArgumentParsing.AudioFileFinder;
import Main.Controller;
import Main.SearchController;
import Print.SearchPrint.SearchPrintAdapter;
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
                .printAdapter(new SearchPrintAdapter(getPrintArguments()))
                .build();
    }

    private void parseSearchArguments(String[] args) {
        CommandLine arguments = makeCommandLine(searchWorkflowArgs);
        CommandLine.ParseResult result = arguments.parseArgs(args);
        ParseErrorHandler.handleErrors(result);
    }

    private CommandLine makeCommandLine(SearchWorkflowArgs args) {
        CommandLine arguments = new CommandLine(args);
        arguments.getCommandSpec().parser().collectErrors(true);
        return arguments;
    }

    private TranscriptArguments getTranscriptArguments() {
        searchWorkflowArgs.model = System.getenv("MP4GREP_MODEL");
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
