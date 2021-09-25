package Main;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedList;
import java.util.List;

@Command(name = "mp4grep")
public class PicoCliArgs {

    @ArgGroup(heading = "Section1", exclusive = true, validate = true)
    HelpSection1 section1 = new HelpSection1();

    static class HelpSection1 {
        @Option(names = {"--before", "-b"}, description = "Words to print before a match (default 5)", defaultValue = "5")
        public int wordsToPrintBeforeMatch;

        @Option(names = {"--after", "-a"}, description = "Words to print after a match (default 5)", defaultValue = "5")
        public int wordsToPrintAfterMatch;

        @Option(required = true, names = {"--model"}, description = "Transcription neural network model directory (if you want to use your own model)", defaultValue = "model")
        public String model = "model";

        @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
        private boolean helpRequested = false;
    }

    @ArgGroup(heading = "Section 2", exclusive = true, validate = true)
    HelpSection2 section2 = new HelpSection2();

    static class HelpSection2 {
        @Parameters(index = "0", arity = "1", paramLabel = "<Search string>")
        String search;

        @Parameters(index = "1..*", arity = "1..*", paramLabel = "<Files/directories>")
        List<String> filesAndDirectories = new LinkedList<>();
    }
}