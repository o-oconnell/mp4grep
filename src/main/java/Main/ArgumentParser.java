package Main;

import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Print.PrintAdapter;
import Search.SearchAdapter;
import Transcribe.TranscriptAdapter;
import Transcribe.VoskAdapter;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class ArgumentParser {
    private final PicoCliArgs argObject;

    public ArgumentParser() {
        argObject = new PicoCliArgs();
    }

    public Grepper getGrepperForArgs(String[] args) {
        parseArguments(args);
        return createGrepper();
    }

    private void parseArguments(String[] args) {
        CommandLine arguments = new CommandLine(argObject);
        collectErrors(arguments);

        ParseResult result = arguments.parseArgs(args);
        handleErrors(result);

        if (arguments.isUsageHelpRequested()) {
            arguments.usage(System.out);
            return;
        }
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
                .files(FileParser.getFileList(argObject.section2.filesAndDirectories))
                .speechToText(getSpeechToText())
                .search(argObject.section2.search)
                .modelDirectory(argObject.section1.model)
                .build();
    }

    private VoskAdapter getSpeechToText() {
        return new VoskAdapter();
    }

    private SearchArguments getSearchArguments() {
        return SearchArguments
                .builder()
                .search(argObject.section2.search)
                .build();
    }

    private PrintArguments getPrintArguments() {
        return PrintArguments
                .builder()
                .wordsAfterMatch(argObject.section1.wordsToPrintAfterMatch)
                .wordsBeforeMatch(argObject.section1.wordsToPrintBeforeMatch)
                .search(argObject.section2.search)
                .build();
    }
}
