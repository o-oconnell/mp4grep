package Main.ArgumentParsing.Parser;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ArgGroup;

import java.util.LinkedList;
import java.util.List;

@Command(name = "mp4grep")
public class TranscribeWorkflowArgs {
    @Parameters(index = "0..*", arity = "1..*", paramLabel = "<files/directories>", description = "Space-separated list of files and directories to grep")
    public List<String> filesAndDirectories = new LinkedList<>();

    @Option(names = {"--words"})
    public int wordsPerLine = 5;

    @Option(required = true, names = {"--model"}, description = "Language model directory", defaultValue = "model", paramLabel = "<model>")
    public String model = "model";

    @ArgGroup(exclusive = true)
    ExclusiveOptions exclusiveOptions = new ExclusiveOptions();

    public static class ExclusiveOptions {
        @Option(names = {"--transcribe"})
        public boolean transcribe = false;

        @Option(names = {"--transcribe-to-files"})
        public boolean transcribeToFiles = false;
    }
}