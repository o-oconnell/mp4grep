package Main.ArgumentParsing;

import Main.ArgumentParsing.Parser.*;
import Main.Controller;

// Responsibility: creates the controller for the correct workflow
// Reason to change: new workflow is added
public class ControllerFactory {
    public Controller getControllerForArgs(String[] args) {
        Parser parser = null;
        if (argsContainHelp(args)) {
            parser = HelpParser
                    .builder()
                    .args(args)
                    .build();
        } else if (argsContainRawWorkflowArgs(args)) {
            System.out.println("--raw or --raw-to-file detected. Ignoring any search arguments.");
            parser = RawParser.builder()
                    .args(args)
                    .build();
        } else {
            parser = SearchParser
                    .builder()
                    .args(args)
                    .build();
        }

        return parser.getController();
    }

    private boolean argsContainHelp(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--help") || args[i].equals("-h")) {
                return true;
            }
        }
        return false;
    }

    private boolean argsContainRawWorkflowArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("--raw") || args[i].equals("--raw-to-file")) {
                return true;
            }
        }
        return false;
    }
}
