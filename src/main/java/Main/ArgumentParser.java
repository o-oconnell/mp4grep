package Main;

import java.util.LinkedList;
import java.util.List;

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

        List<String> inputFilesDirs = getListOfFilesDirs(argObject.filesAndDirectories);
        SpeechToText speechToText = createSpeechToText();


        return new Grepper.GrepperBuilder()
                .speechToText(speechToText)
                .searchString(argObject.searchString)
                .isRegexSearch(argObject.regex)
                .inputFilesDirs(inputFilesDirs)
                .build();
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

    private List<String> convertInputToList(String inputFilesDirs) {

        System.out.println(inputFilesDirs);
        String[] splitBySpaces = inputFilesDirs.split(" ");

        List<String> result = null;

        for (String s : splitBySpaces) {
            result.add(s);
        }

        return result;
    }

    private boolean stringArgIsSet(String arg) {
        return arg != null;
    }

    // TODO: select different speechtotext based on lang, print error message and exit if matching engine for language doesn't exist
    // (will require downloading models if they are not already installed)
    private SpeechToText getSpeechToTextForLanguage(String language) {
        return new VoskSpeechToText();
    }


}
