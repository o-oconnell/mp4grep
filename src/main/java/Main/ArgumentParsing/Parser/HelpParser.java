package Main.ArgumentParsing.Parser;

import Globals.GlobalErrorCodes;
import Main.Controller;
import Main.RawTranscriptController;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public class HelpParser implements Parser {
    @NonNull private String[] args;
    private static final List<String> helpMessage = List.of(
            "Usage: mp4grep [options] [search string] [files/directories]",
            "--before/--after           Number of words to print before/after a match (default 5)",
            "--model [model directory]  Vosk-compatible speech recognition model (default \"model\")",
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
