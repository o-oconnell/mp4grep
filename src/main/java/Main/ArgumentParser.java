package Main;

import java.util.LinkedList;
import java.util.List;

import Arguments.GrepperArguments;
import Arguments.SpeechToTextArguments;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine;

import Search.Grepper;
import SpeechToText.SpeechToText;
import SpeechToText.VoskSpeechToText;

public class ArgumentParser {

    private final Args argObject;

    private class Args {

        @Parameters(index = "0")
        String searchString;

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

    private List<String> getListOfFilesDirs(List<String> inputFilesDirs) {
        if (inputFilesDirs == null)
            return new LinkedList<String>();
        else
            return inputFilesDirs;
    }

    private SpeechToText createSpeechToText() {

        if (stringArgIsSet(argObject.language)) {
            return getSpeechToTextForLanguage(argObject.language);
        } else {
            return new VoskSpeechToText();
        }
    }

    private SpeechToText getSpeechToTextForLanguage(String language) {
        return new VoskSpeechToText();
    }

    private boolean stringArgIsSet(String arg) {
        return arg != null;
    }

    private SpeechToTextArguments getSpeechToTextArguments() {
        return SpeechToTextArguments
                .builder()
                .wordsToPrintBeforeMatch(argObject.wordsToPrintBeforeMatch)
                .wordsToPrintAfterMatch(argObject.wordsToPrintAfterMatch)
                .build();
    }

    private GrepperArguments getGrepperArguments() {
        List<String> inputFilesDirs = getListOfFilesDirs(argObject.filesAndDirectories);
        SpeechToText speechToText = createSpeechToText();
        SpeechToTextArguments speechToTextArguments = getSpeechToTextArguments();

        return GrepperArguments
                .builder()
                .speechToText(speechToText)
                .searchString(argObject.searchString)
                .inputFilesDirs(inputFilesDirs)
                .speechToTextArguments(speechToTextArguments)
                .build();
    }
}
