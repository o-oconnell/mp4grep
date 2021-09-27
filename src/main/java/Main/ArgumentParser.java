package Main;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Main.PicoCliArguments.MP4GrepArgs;
import Main.PicoCliArguments.UsageHelpArgs;
import Print.PrintAdapter;
import Search.SearchAdapter;
import Transcribe.TranscriptAdapter;
import Transcribe.VoskAdapter;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class ArgumentParser {
    private static final int ERROR_EXIT_CODE = 1;
    private final MP4GrepArgs mp4GrepArgs;

    public ArgumentParser() {
        mp4GrepArgs = new MP4GrepArgs();
    }

    public Grepper getGrepperForArgs(String[] args) {
        parseArguments(args);
        return createGrepper();
    }

    private void parseArguments(String[] args) {
        CommandLine arguments = makeCommandLine(mp4GrepArgs);
        ParseResult result = arguments.parseArgs(args);
        handleErrors(result);

        if (arguments.isUsageHelpRequested()) {
            printUsageHelp();
        }
    }

    private CommandLine makeCommandLine(MP4GrepArgs args) {
        CommandLine arguments = new CommandLine(args);
        arguments.getCommandSpec().parser().collectErrors(true);
        return arguments;
    }

    private void handleErrors(ParseResult result) {
        if (!result.errors().isEmpty()) {
            System.out.println("Usage: mp4grep [options] [search string] [files/directories]");
            System.out.println("Try \'mp4grep --help\' for more information");
            System.exit(ERROR_EXIT_CODE);
        }
    }

    private void printUsageHelp() {
        UsageHelpArgs helpArgs = new UsageHelpArgs();
        CommandLine helpArguments = new CommandLine(helpArgs);
        helpArguments.usage(System.out);
    }

    public Grepper createGrepper() {
        return Grepper
                .builder()
                .transcriptionAdapter(getTranscriptionAdapter())
                .searchAdapter(getSearchAdapter())
                .printAdapter(getPrintAdapter())
                .build();
    }

    private TranscriptAdapter getTranscriptionAdapter() {
        return new TranscriptAdapter(getTranscriptArguments());
    }

    private SearchAdapter getSearchAdapter() {
        return new SearchAdapter(getSearchArguments());
    }

    private PrintAdapter getPrintAdapter() {
        return new PrintAdapter(getPrintArguments());
    }

    private TranscriptArguments getTranscriptArguments() {
        return TranscriptArguments
                .builder()
                .files(FileParser.getFileList(mp4GrepArgs.filesAndDirectories))
                .speechToText(getSpeechToText())
                .search(mp4GrepArgs.search)
                .modelDirectory(mp4GrepArgs.model)
                .build();
    }

    private VoskAdapter getSpeechToText() {
        return new VoskAdapter();
    }

    private SearchArguments getSearchArguments() {
        return SearchArguments
                .builder()
                .search(mp4GrepArgs.search)
                .build();
    }

    private PrintArguments getPrintArguments() {
        return PrintArguments
                .builder()
                .wordsAfterMatch(mp4GrepArgs.wordsToPrintAfterMatch)
                .wordsBeforeMatch(mp4GrepArgs.wordsToPrintBeforeMatch)
                .search(mp4GrepArgs.search)
                .build();
    }
}
