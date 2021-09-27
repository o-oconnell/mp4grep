package Main.PicoCliArguments;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedList;
import java.util.List;

@Command(name = "mp4grep")
public class UsageHelpArgs {
    @ArgGroup(heading = "\n")
    public HelpSection1 section1 = new HelpSection1();

    static class HelpSection1 {
        @Option(names = {"--before", "-b"}, description = "Words to print before a match (default 5)", defaultValue = "5", paramLabel = "<words>")
        public int wordsToPrintBeforeMatch;

        @Option(names = {"--after", "-a"}, description = "Words to print after a match (default 5)", defaultValue = "5", paramLabel = "<words>")
        public int wordsToPrintAfterMatch;

        @Option(required = true, names = {"--model"}, description = "Language model directory", defaultValue = "model", paramLabel = "<model>")
        public String model = "model";

        @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
        private boolean helpRequested = false;
    }

    @ArgGroup(heading = "")
    public HelpSection2 section2 = new HelpSection2();

    static class HelpSection2 {
        @Parameters(index = "0", arity = "1", paramLabel = "<search string>", description = "Pattern to find in audio/video file transcriptions")
        public String search;

        @Parameters(index = "1", arity = "1", paramLabel = "<files/directories>", description = "Space-separated list of files and directories to grep")
        public List<String> filesAndDirectories = new LinkedList<>();
    }
}