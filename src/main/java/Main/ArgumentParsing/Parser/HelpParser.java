package Main.ArgumentParsing.Parser;

import Globals.GlobalErrorCodes;
import Main.Controller;
import Main.RawTranscriptController;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class HelpParser implements Parser {
    @NonNull private String[] args;

    @Override
    public Controller getController() {
        System.out.println("Help message goes here");
        System.exit(GlobalErrorCodes.ERROR_EXIT_CODE);
        return RawTranscriptController.builder().build();
    }
}
