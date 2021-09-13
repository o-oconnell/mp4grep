package Transcription;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class VoskAdapter {
    private static final int SAMPLING_RATE = 16000;
    private static final String MODEL_DIRECTORY = "model/vosk-model-LARGE-en-uszz";
    private static final int AUDIO_BYTE_ARRAY_SIZE = 4086;
    private CacheInfo cacheInfo;

    public VoskAdapter() {
        voskSetup();
    }

    private void voskSetup() {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
    }

    public void transcribeAudio(CacheInfo cacheInfo) {
        this.cacheInfo = cacheInfo;
        String convertedAudioFile = VoskConverter.convertToVoskFormat(cacheInfo.inputFilename);
        writeTranscriptAndTimestamps(convertedAudioFile, cacheInfo);
    }

    private void writeTranscriptAndTimestamps(String audioFileName, CacheInfo cacheInfo) {
        InputStream audioInputStream = createInputStream(audioFileName);
        Recognizer recognizer = createRecognizer();

        createOutputFiles(cacheInfo);
        writeMainRecognizerResults(recognizer, audioInputStream);
        writeFinalRecognizerResult(recognizer);
    }

    private InputStream createInputStream(String audioFileName) {
        InputStream AudioInputStream = null;
        try {
            FileInputStream fileInput = getFileInputStream(audioFileName);
            BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);
            AudioInputStream = AudioSystem.getAudioInputStream(bufferedInput);
        } catch (UnsupportedAudioFileException | IOException e ) {
            System.out.println("Audio file not found or audio file unsupported");
        }

        return AudioInputStream;
    }

    private FileInputStream getFileInputStream(String audioFileName) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(audioFileName);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Failed to create new files stream from audio filename");
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
        Path modelPath = Paths.get(MODEL_DIRECTORY);
        if (Files.exists(modelPath)) {
            return new Model(MODEL_DIRECTORY);
        } else {
            System.out.println("Model directory " + MODEL_DIRECTORY + " not found. Exiting.");
            System.exit(1);
            return new Model(MODEL_DIRECTORY);
        }
    }

    private void createOutputFiles(CacheInfo cacheInfo) {
        createFile(cacheInfo.timestampFilename);
        createFile(cacheInfo.transcriptFilename);
    }

    private void writeMainRecognizerResults(Recognizer recognizer, InputStream audioInputStream) {
        int numberBytes;
        byte[] audioBuffer = new byte[AUDIO_BYTE_ARRAY_SIZE];

        numberBytes = writeAudioToBuffer(audioBuffer, audioInputStream);

        while (numberBytes >= 0) {
            if (recognizer.acceptWaveForm(audioBuffer, numberBytes)) {
                String transcriptJson = recognizer.getResult();
                printWordsTimestampsToTempFiles(transcriptJson);
            }

            numberBytes = writeAudioToBuffer(audioBuffer, audioInputStream);
        }
    }

    private int writeAudioToBuffer(byte[] inputBuffer, InputStream audioInputStream) {
        Integer result = null;

        try {
            result = audioInputStream.read(inputBuffer);
        } catch (java.io.IOException e) {
            System.out.println("Error reading bytes from the audio files stream (derived from the audio file)");
            e.printStackTrace();
        }
        return result;
    }

    // TODO: change this name
    private void printWordsTimestampsToTempFiles(String jsonInput) {
        JsonObject jsonParseObject = getJsonObject(jsonInput);

        if (jsonParseObject.has("result")) {
            JsonArray allTimestampedWords = getAllWords(jsonParseObject);
            for (JsonElement wordInfo : allTimestampedWords) {
                    String startTime = getStringAttribute("start", wordInfo);
                    String word = getStringAttribute("word", wordInfo);
                    putWordInTempFile(word);
                    putTimestampInTempFile(startTime);
            }
        } else {
            // TODO: make own exception for this, log the exception, and print
            // different output to the user that is less verbose
            // (maybe make this function do error handling around another function).
        }
    }

    private JsonObject getJsonObject(String input) {
        return new Gson().fromJson(input, JsonObject.class);
    }

    private JsonArray getAllWords(JsonObject jsonObject) {
        return jsonObject.get("result").getAsJsonArray();
    }

    private String getStringAttribute(String string, JsonElement wordInfo) {
        return wordInfo.getAsJsonObject().get(string).getAsString();
    }

    // TODO: change to pass filename as a parameter, also change to pass cacheKey as a parameter
    private void putWordInTempFile(String word) {
        try {
            String transcriptFilename = voskAdapterArguments.transcriptFilename;
            Path outputFile = Path.of(transcriptFilename);
            Files.writeString(outputFile, appendNewlineTo(word), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing sound translation to temporary file");
            e.printStackTrace();
        }
    }

    private void putTimestampInTempFile(String timestamp) {
        try {
            String timestampFilename = voskAdapterArguments.timestampFilename;
            Path outputFile = Path.of(timestampFilename);
            Files.writeString(outputFile, appendNewlineTo(timestamp), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing sound translation to temporary file");
        }
    }

    private void createFile(String filename) {
        try {
            File transcript = new File(filename);
            transcript.delete();
            transcript.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating transcript file.");
            e.printStackTrace();
        }
    }

    private void writeFinalRecognizerResult(Recognizer recognizer) {
        String finalJsonResult = recognizer.getFinalResult();
        printWordsTimestampsToTempFiles(finalJsonResult);
    }

    // TODO: logan thought this sucked
    private String appendNewlineTo(String string) {
        return string + '\n';
    }
}
