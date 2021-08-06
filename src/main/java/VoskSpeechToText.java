import org.unix4j.io.FileInput;

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


public class VoskSpeechToText implements SpeechToText {

    private static final String TEMP_AUDIO_STORAGE_FILE = ".mp4grep_tmp";
    private static final int SAMPLING_RATE = 16000;
    private static final String MODEL_DIRECTORY = "model";
    private static final int AUDIO_BYTE_ARRAY_SIZE = 4086;
    String audioFileName;

    public VoskSpeechToText(String audioFileName) {
        this.audioFileName = audioFileName;
        voskSetup();
    }

    public void voskSetup() {
        LibVosk.setLogLevel(LogLevel.DEBUG);
    }

    @Override
    public Greppable getGreppableResult() {

        sendAudioToTimestampedFile();

        return new Unix4JGreppable(TEMP_AUDIO_STORAGE_FILE);
    }

    private void sendAudioToTimestampedFile() {

        InputStream audioInputStream = createInputStream();
        Recognizer recognizer = createRecognizer();

        printMainRecognizerResults(recognizer, audioInputStream);
        printFinalRecognizerResult(recognizer);
    }

    private InputStream createInputStream() {

        InputStream AudioInputStream = null;
        try {
            FileInputStream fileInput = getFileInputStream();
            BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
            AudioInputStream = AudioSystem.getAudioInputStream(bufferedInput);
        } catch (UnsupportedAudioFileException | IOException e ) {
            System.out.println("Audio file not found or audio file unsupported");
        }

        return AudioInputStream;
    }

    private FileInputStream getFileInputStream() {

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(audioFileName);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Failed to create new input stream from audio filename");
        }

        return inputStream;
    }

    private Recognizer createRecognizer() {
        return new Recognizer(
                createModel(),
                SAMPLING_RATE
        );
    }

    private Model createModel() {
        return new Model(MODEL_DIRECTORY);
    }

    private void printMainRecognizerResults(Recognizer recognizer, InputStream audioInputStream) {
        int numberBytes;
        byte[] inputBuffer = new byte[AUDIO_BYTE_ARRAY_SIZE];

        numberBytes = readBytesFromInputStream(audioInputStream, inputBuffer);

        while (numberBytes >= 0) {
            if (recognizer.acceptWaveForm(inputBuffer, numberBytes)) {
                String jsonResult = recognizer.getResult();
                printTimestampedResultToOutputFile(jsonResult);
            }

            numberBytes = readBytesFromInputStream(audioInputStream, inputBuffer);
        }
    }

    private int readBytesFromInputStream(InputStream audioInputStream, byte[] inputBuffer) {
        return readStreamIntoInputBuffer(inputBuffer, audioInputStream);
    }

    private int readStreamIntoInputBuffer(byte[] inputBuffer, InputStream audioInputStream) {
        Integer result = null;

        try {
            result = audioInputStream.read(inputBuffer);
        } catch (java.io.IOException e) {
            System.out.println("Error reading bytes from the audio input stream (derived from the audio file)");
        }
        return result;
    }

    private void printFinalRecognizerResult(Recognizer recognizer) {
        String finalJsonResult = recognizer.getFinalResult();
        printTimestampedResultToOutputFile(finalJsonResult);
    }

    private void printTimestampedResultToOutputFile(String jsonResult) {

        try {
            clearTempAudioFile();
            Path outputFile = Path.of(TEMP_AUDIO_STORAGE_FILE);
            String content = getTimestampFromJSONString(jsonResult) + getSoundTranslationFromJSONString(jsonResult) + "\n";
            Files.writeString(outputFile, content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing sound translation to temporary file");
        }
    }

    private void clearTempAudioFile() {

        try {
            File tempFile = new File(TEMP_AUDIO_STORAGE_FILE);
            tempFile.delete();
            tempFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating temporary sound file");
        }
    }

    // not yet refactored, since formatting output will probably require another module
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
