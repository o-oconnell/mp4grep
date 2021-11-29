package Main.ArgumentParsing.Parser;

import Globals.GlobalErrorCodes;
import Main.Controller;
import Main.RawTranscriptController;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

import static Globals.GlobalColors.ANSI_RESET;
import static Globals.GlobalColors.ANSI_YELLOW;

@Builder
public class HelpParser implements Parser {
    @NonNull private String[] args;
    private static final List<String> helpMessage = List.of(
            "Usage: mp4grep [options] [search string] [files/directories] | --clear-cache",
            "--before/--after           Number of words to print before/after a match",
            "--model [model directory]  Vosk-compatible speech recognition model",
            "--clear-cache              Delete cached transcriptions (other arguments are ignored)",
            "--transcribe               Print transcription to console",
            "--transcribe-to-file       Transcribe and save text files to your local directory",
            "[search string]            Regex-optional query",
            "[files/directories]        Space-separated list of files/directories to search"
    );

    @Override
    public Controller getController() {
        for (String line : helpMessage) {
            System.out.println(line);
        }
        System.exit(GlobalErrorCodes.SUCCESS_EXIT_CODE);
        return RawTranscriptController.builder().build();
    }
}
