package Main.ArgumentParsing;

import Globals.GlobalArgs;
import Main.ArgumentParsing.Parser.*;
import Main.Controller;
import Transcribe.Cache.TranscriptCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Globals.GlobalArgs.*;

// Responsibility: creates the controller for the correct workflow
// Reason to change: new workflow is added
public class ControllerFactory {
    public Controller getControllerForArgs(String[] args) {
        Parser parser = null;
        if (argsContains(HELP_ANY, args)) {
            parser = HelpParser
                    .builder()
                    .args(args)
                    .build();
        } else if (argsContains(TRANSCRIBE_ANY, args)) {
            parser = TranscribeParser
                    .builder()
                    .args(args)
                    .build();
        } else if (argsContains(CLEAR_CACHE, args)) {
            System.out.println("Clearing cache.");
            TranscriptCache.clearCacheFiles();
            System.exit(0);
        } else {
            parser = SearchParser
                    .builder()
                    .args(args)
                    .build();
        }

        return parser.getController();
    }

    private boolean argsContains(String value, String[] args) {
        List<String> argsList = getList(args);
        if (argsList.contains(value)) {
            return true;
        }
        return false;
    }

    private boolean argsContains(String[] values, String[] args) {
        List<String> argsList = getList(args);
        for (String value : values) {
            if (argsList.contains(value))
                return true;
        }
        return false;
    }

    private List<String> getList(String[] args) {
        return new ArrayList<>(Arrays.asList(args));
    }
}
