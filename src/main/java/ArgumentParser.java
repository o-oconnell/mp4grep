import com.beust.jcommander.*;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {

    private JCommanderArgs argObject;

    public ArgumentParser() {
        argObject = new JCommanderArgs();
    }

    public Grepper getGrepperForArgs(String[] args) {

        parseArguments(args);
        return createGrepper();
    }

    private void parseArguments(String[] args) {

        JCommander.newBuilder()
                .addObject(argObject)
                .build()
                .parse(args);
    }

    private Grepper createGrepper() {

        String searchString = argObject.getSearchString();
        String filename = argObject.getInput();

        SpeechToText speechToText = createSpeechToText(filename);

        return new Grepper.GrepperBuilder()
                .speechToText(speechToText)
                .searchString(searchString)
                .build();
    }

    private SpeechToText createSpeechToText(String filename) {
        return new VoskSpeechToText(filename);
    }
}
