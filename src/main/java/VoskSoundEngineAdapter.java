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
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class VoskSoundEngineAdapter implements SoundEngineAdapter {

    final String tempAudioStorageFile = ".mp4grep_tmp";
    final String modelDirectory = "model";
    String audioFileName;

    public VoskSoundEngineAdapter(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    @Override
    public Greppable getGreppableResult() {

        sendAudioToTimestampedFile();
        return new GreppableFile(tempAudioStorageFile);
    }

    private void sendAudioToTimestampedFile() {

        LibVosk.setLogLevel(LogLevel.DEBUG);

        Model model = createModel();
        InputStream ais = createInputStream();
        Recognizer recognizer = createRecognizer(model); // requires that model be created prior - temporal coupling, but never called elsewhere

        int nbytes;
        byte[] b = new byte[4096];
        while ((nbytes = readBytesFromInputStream(ais, b)) >= 0) {
            if (recognizer.acceptWaveForm(b, nbytes)) {

                String jsonResult = recognizer.getResult();
                // printTimestampedResultToConsole(jsonResult);
                    printTimestampedResultToOutputFile(jsonResult);
            }
        }

        // Vosk implementation detail, must call getFinalResult() to get the last string of text from the audio
        String finalJsonResult = recognizer.getFinalResult();
        printTimestampedResultToOutputFile(finalJsonResult);
    }

    private int readBytesFromInputStream(InputStream ais, byte[] b) {
        Integer nbytes = null;

        try {
            nbytes = ais.read(b);
        } catch (IOException e) {
            System.out.println("Error reading bytes from the input stream");
        }
        return nbytes; // returns the number of bytes read into the buffer
    }

    private Model createModel() {

            Model model = new Model(modelDirectory);
            return model;
    }

    private InputStream createInputStream() {

        InputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(audioFileName)));
        } catch (UnsupportedAudioFileException | IOException e ) {
            System.out.println("Audio file not found or audio file unsupported");
        }

        return ais;
    }

    private Recognizer createRecognizer(Model model) {
        Recognizer recognizer = new Recognizer(model, 16000);
        return recognizer;
    }

    private void printTimestampedResultToConsole(String jsonResult) {

            String content = getTimestampFromJSONString(jsonResult) + getSoundTranslationFromJSONString(jsonResult) + "\n";
            System.out.println(content);
    }

    private void printTimestampedResultToOutputFile(String jsonResult) {

        try {
            clearTempAudioFile();
            Path outputFile = Path.of(tempAudioStorageFile);
            String content = getTimestampFromJSONString(jsonResult) + getSoundTranslationFromJSONString(jsonResult) + "\n";
            Files.writeString(outputFile, content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing sound translation to temporary file");
        }
    }

    private void clearTempAudioFile() {

        try {
            File tempFile = new File(tempAudioStorageFile);
            tempFile.delete();
            tempFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating temporary sound file");
        }
    }

    private String getTimestampFromJSONString(String input) {

        Object jsonParseObject = Configuration.defaultConfiguration().jsonProvider().parse(input);
        double timestampStart = JsonPath.read(jsonParseObject, "$.result[0].start");
        double timestampEnd = JsonPath.read(jsonParseObject, "$.result[-1].end");

        return String.format("%.2f---%.2f:", timestampStart, timestampEnd);
    }

    private String getSoundTranslationFromJSONString(String input) {

        Object jsonParseObject = Configuration.defaultConfiguration().jsonProvider().parse(input);
        String text = JsonPath.read(jsonParseObject, "$.text");
        return text;
    }
}
