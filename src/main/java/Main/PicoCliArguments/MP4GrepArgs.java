package Main.PicoCliArguments;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedList;
import java.util.List;

@Command(name = "mp4grep")
public class MP4GrepArgs {
    @Option(names = {"--before", "-b"}, description = "Words to print before a match (default 5)", defaultValue = "5")
    public int wordsToPrintBeforeMatch;

    @Option(names = {"--after", "-a"}, description = "Words to print after a match (default 5)", defaultValue = "5")
    public int wordsToPrintAfterMatch;

    @Option(required = true, names = {"--model"}, description = "Transcription neural network model directory (if you want to use your own model)", defaultValue = "model")
    public String model = "model";

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
    private boolean helpRequested = false;

    @Parameters(index = "0", arity = "1", paramLabel = "<Search string>")
    public String search;

    @Parameters(index = "1..*", arity = "1..*", paramLabel = "<Files/directories>")
    public List<String> filesAndDirectories = new LinkedList<>();
}