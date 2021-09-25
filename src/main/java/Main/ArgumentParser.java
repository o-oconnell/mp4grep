package Main;

import java.util.List;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Print.PrintAdapter;
import Search.SearchAdapter;
import Transcribe.TranscriptAdapter;
import Transcribe.VoskAdapter;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine;

public class ArgumentParser {
    private final Args argObject;

    private class Args {
        @Parameters(index = "0", arity = "1")
        String search;

        @Parameters(index = "1..*", arity = "1..*")
        List<String> filesAndDirectories;

        @Option(names = {"--before", "-b"}, description = "Words to print before a match (default 5)")
        public int wordsToPrintBeforeMatch = 5;

        @Option(names = {"--after", "-a"}, description = "Words to print after a match (default 5)")
        public int wordsToPrintAfterMatch = 5;

        @Option(names = {"--model"}, description = "Transcription neural network model directory (if you want to use your own model)")
        public String model = "model";

        @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;
    }

    public ArgumentParser() {
        argObject = new Args();
    }

    public Grepper getGrepperForArgs(String[] args) {

        parseArguments(args);
        return createGrepper();
    }

    private void parseArguments(String[] args) {

        CommandLine commandLine = new CommandLine(argObject);
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else {

        }

//        CommandLine arguments = new CommandLine(argObject);
//        collectErrors(arguments);
//        ParseResult result = arguments.parseArgs(args);
//        handleErrors(result);
    }

    private void collectErrors(CommandLine arguments) {
        arguments.getCommandSpec().parser().collectErrors(true);
    }

    private void handleErrors(ParseResult result) {
        if (!result.errors().isEmpty()) {
            System.out.println("Usage: mp4grep [options] [search string] [files/directories]");
            System.out.println("Try \'mp4grep --help\' for more information");
            System.exit(1);
        }
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
                .files(FileParser.getFileList(argObject.filesAndDirectories))
                .speechToText(getSpeechToText())
                .search(argObject.search)
                .modelDirectory(argObject.model)
                .build();
    }

    private VoskAdapter getSpeechToText() {
        return new VoskAdapter();
    }

    private SearchArguments getSearchArguments() {
        return SearchArguments
                .builder()
                .search(argObject.search)
                .build();
    }

    private PrintArguments getPrintArguments() {
        return PrintArguments
                .builder()
                .wordsAfterMatch(argObject.wordsToPrintAfterMatch)
                .wordsBeforeMatch(argObject.wordsToPrintBeforeMatch)
                .search(argObject.search)
                .build();
    }
}
