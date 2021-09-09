package Main;

import java.util.List;

import Arguments.GrepperArguments;
import Arguments.PrintArguments;
import Arguments.SearchArguments;
import Arguments.TranscriptArguments;
import Transcription.VoskAdapter;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine;

public class ArgumentParser {
    private final Args argObject;

    private class Args {
        @Parameters(index = "0")
        String search;

        @Parameters(index = "1..*")
        List<String> filesAndDirectories;

        @Option(names = {"--regex", "-r"}, description = "Flag to toggle regex searching", defaultValue = "false")
        public boolean regex;

        @Option(names = {"--language", "-l"}, description = "Set the audio transcription language")
        public String language;

        @Option(names = {"--recursive", "-R"}, description = "Search recursively inside of a provided directory.")
        public boolean recursive;

        @Option(names = {"--timestamp", "-t"}, description = "The length of each timestamp in seconds")
        public int timestampSeconds;

        @Option(names = {"--count", "-c"}, description = "Output a count of matching words/phrases only")
        public boolean count;

        @Option(names = {"--before", "-b"}, description = "Words to print before a match (default 5)")
        public int wordsToPrintBeforeMatch = 5;

        @Option(names = {"--after", "-a"}, description = "Words to print after a match (default 5)")
        public int wordsToPrintAfterMatch = 5;
    }

    public ArgumentParser() {
        argObject = new Args();
    }

    public Grepper getGrepperForArgs(String[] args) {
        parseArguments(args);
        return createGrepper();
    }

    private void parseArguments(String[] args) {
        CommandLine arguments = new CommandLine(argObject);
        arguments.parseArgs(args);
    }

    private Grepper createGrepper() {
        GrepperArguments grepperArguments = getGrepperArguments();
        return new Grepper(grepperArguments);
    }

    private GrepperArguments getGrepperArguments() {
        TranscriptArguments transcriptArguments = getTranscriptArguments();
        SearchArguments searchArguments = getSearchArguments();
        PrintArguments printArguments = getPrintArguments();

        return GrepperArguments
                .builder()
                .transcriptArguments(transcriptArguments)
                .searchArguments(searchArguments)
                .printArguments(printArguments)
                .build();
    }

    private TranscriptArguments getTranscriptArguments() {
        VoskAdapter speechToText = new VoskAdapter();
        List<String> files = FileParser.getFileList(argObject.filesAndDirectories);

        return TranscriptArguments
                .builder()
                .files(files)
                .speechToText(speechToText)
                .search(argObject.search)
                .build();
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