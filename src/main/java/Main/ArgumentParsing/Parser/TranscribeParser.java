package Main.ArgumentParsing.Parser;

import Arguments.RawPrintArguments;
import Arguments.TranscriptArguments;
import Globals.GlobalErrorCodes;
import Main.ArgumentParsing.AudioFileFinder;
import Main.Controller;
import Main.RawTranscriptController;
import Print.RawPrintAdapter;
import Transcribe.TranscriptAdapter;
import Transcribe.VoskAdapter;
import lombok.Builder;
import lombok.NonNull;
import picocli.CommandLine;

@Builder
public class TranscribeParser implements Parser {
    @NonNull private String[] args;
    private static final TranscribeWorkflowArgs rawWorkflowArgs = new TranscribeWorkflowArgs();

    public Controller getController() {
        parseRawArguments(args);
        return RawTranscriptController
                .builder()
                .rawPrintAdapter(new RawPrintAdapter(getRawPrintArguments()))
                .transcriptAdapter(new TranscriptAdapter(getRawTranscriptArguments()))
                .build();
    }

    private void parseRawArguments(String[] args) {
        CommandLine arguments = makeCommandLine(rawWorkflowArgs);
        CommandLine.ParseResult result = arguments.parseArgs(args);
        ParseErrorHandler.handleErrors(result);
    }

    private CommandLine makeCommandLine(TranscribeWorkflowArgs args) {
        CommandLine arguments = new CommandLine(args);
        arguments.getCommandSpec().parser().collectErrors(true);
        return arguments;
    }

    private RawPrintArguments getRawPrintArguments() {
        if (rawWorkflowArgs.wordsPerLine <= 0) {
            System.out.println("Words per line must be greater than 0. Exiting.");
            System.exit(GlobalErrorCodes.ERROR_EXIT_CODE);
        }
        return RawPrintArguments
                .builder()
                .printToFiles(rawWorkflowArgs.exclusiveOptions.transcribeToFiles)
                .wordsPerLine(rawWorkflowArgs.wordsPerLine)
                .build();
    }

    private TranscriptArguments getRawTranscriptArguments() {
        return TranscriptArguments
                .builder()
                .files(AudioFileFinder.getFileList(rawWorkflowArgs.filesAndDirectories))
                .modelDirectory(rawWorkflowArgs.model)
                .speechToText(getSpeechToText())
                .build();
    }

    private VoskAdapter getSpeechToText() {
        return new VoskAdapter();
    }
}
