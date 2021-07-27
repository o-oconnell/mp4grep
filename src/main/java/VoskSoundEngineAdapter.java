import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import com.jayway.jsonpath.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VoskSoundEngineAdapter {

    String tempAudioStorageFile = ".mp4grep_tmp";
    String audioFileName;
    String modelDirectory = "model";

    public VoskSoundEngineAdapter() {

    }

    public void parseAudioFile(String audioFileName) throws IOException, UnsupportedAudioFileException {
        this.audioFileName = audioFileName;
        sendAudioToTimestampedFile();
    }

    private void sendAudioToTimestampedFile() throws IOException, UnsupportedAudioFileException {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        try (Model model = new Model("model");
             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(audioFileName)));
             Recognizer recognizer = new Recognizer(model, 16000)) {

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                   // String result = recognizer.getResult();

                    String jsonResult = recognizer.getResult();

                    System.out.println(getTimestampFromJSONString(jsonResult));
                    System.out.println(getSoundTranslationFromJSONString(jsonResult));

                } else {
                    // System.out.println(recognizer.getPartialResult());
                }
            }
            System.out.println(recognizer.getFinalResult());
        }
    }

    private String getTimestampFromJSONString(String input) {

        Object jsonParseObject = Configuration.defaultConfiguration().jsonProvider().parse(input);
        double timestampStart = JsonPath.read(jsonParseObject, "$.result[0].start");
        double timestampEnd = JsonPath.read(jsonParseObject, "$.result[-1].end");

        return String.format("Start: %.2f sec; End: %.2f sec;", timestampStart, timestampEnd);
    }

    private String getSoundTranslationFromJSONString(String input) {

        Object jsonParseObject = Configuration.defaultConfiguration().jsonProvider().parse(input);
        String text = JsonPath.read(jsonParseObject, "$.text");
        return text;
    }
}
