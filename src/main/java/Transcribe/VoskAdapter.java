package Transcribe;

import Transcribe.Cache.CacheInfo;
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
import java.util.concurrent.TimeUnit;

public class VoskAdapter {
    private static final int SAMPLING_RATE = 16000;
    private static final String SPLIT_STRING_ON_PERIOD_REGEX = "\\.";
    private static final int AUDIO_BYTE_ARRAY_SIZE = 4086;

    public VoskAdapter() {
        voskSetup();
    }

    private void voskSetup() {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
    }

    public void transcribeAudio(CacheInfo cacheInfo) {
        Recognizer recognizer = createRecognizer(cacheInfo.modelDirectory);
        createOutputFiles(cacheInfo);
        writeMainRecognizerResults(recognizer, cacheInfo);
        writeFinalRecognizerResult(recognizer, cacheInfo);
    }

    private Recognizer createRecognizer(String modelDirectory) {
        return new Recognizer(
                createModel(modelDirectory),
                SAMPLING_RATE
        );
    }

    private Model createModel(String modelDirectory) {
        Path modelPath = Paths.get(modelDirectory);
        if (Files.exists(modelPath)) {
            return new Model(modelDirectory);
        } else {
            System.out.println("Model directory \"" + modelDirectory + "\" not found. Exiting.");
            System.exit(1);
            return new Model(modelDirectory);
        }
    }

    private void createOutputFiles(CacheInfo cacheInfo) {
        createBlankFile(cacheInfo.timestampFilename);
        createBlankFile(cacheInfo.transcriptFilename);
    }

    private void createBlankFile(String filename) {
        try {
            File transcript = new File(filename);
            transcript.delete();
            transcript.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating transcript file.");
            e.printStackTrace();
        }
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

    private void writeMainRecognizerResults(Recognizer recognizer, CacheInfo cacheInfo) {
        String audioFileName = VoskConverter.convertToVoskFormat(cacheInfo.inputFilename);
        InputStream audioInputStream = createInputStream(audioFileName);
        byte[] audioBuffer = new byte[AUDIO_BYTE_ARRAY_SIZE];

        int numberBytes = writeAudioToBuffer(audioBuffer, audioInputStream);

        while (numberBytes >= 0) {
            if (recognizer.acceptWaveForm(audioBuffer, numberBytes)) {
                String transcriptJson = recognizer.getResult();
                writeToCacheFiles(transcriptJson,cacheInfo);
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

    private void writeToCacheFiles(String jsonInput, CacheInfo cacheInfo) {
        JsonObject jsonParseObject = getJsonObject(jsonInput);

        if (jsonParseObject.has("result")) {
            JsonArray allTimestampedWords = getAllWords(jsonParseObject);
            for (JsonElement wordInfo : allTimestampedWords) {
                String startTime = getStringAttribute("start", wordInfo);
                String word = getStringAttribute("word", wordInfo);
                writeToFile(word, cacheInfo.transcriptFilename);
                writeToFile(getTimestampFormat(startTime), cacheInfo.timestampFilename);
            }
        } else {
            // Do nothing. No audio was transcribed.
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

    private void writeToFile(String word, String filename) {
        try {
            Path outputFile = Path.of(filename);
            Files.writeString(outputFile, word + '\n', StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing sound translation to temporary file");
            e.printStackTrace();
        }
    }

    private String getTimestampFormat(String seconds) {
        // edge case since a long cannot consist only of zeros
        if (containsOnlyZeros(removeFraction(seconds))) {
            return "0" + ":" + "00";
        } else {
            Long secondsNum = Long.valueOf(removeFraction(seconds));
            return getHours(secondsNum) + getMinutes(secondsNum) + getSeconds(secondsNum);
        }
    }

    String removeFraction(String string) {
        String[] split = string.split(SPLIT_STRING_ON_PERIOD_REGEX);

        if (split.length > 0) {
            return split[0];
        } else {
            return string;
        }
    }

    private boolean containsOnlyZeros(String input) {
        return input.chars()
                .asLongStream()
                .allMatch(ch -> ch == '.' || ch == '0' || ch == ' ');
    }

    private String getHours(long secondsNum) {
        long hours =  TimeUnit.SECONDS.toHours(secondsNum);
        if (hours > 0) {
            return String.valueOf(hours) + ":";
        } else {
            return "";
        }
    }

    private String getMinutes(long secondsNum) {
        return String.valueOf(TimeUnit.SECONDS.toMinutes(secondsNum) - (TimeUnit.SECONDS.toHours(secondsNum)* 60) + ":");
    }

    private String getSeconds(long secondsNum) {
        long seconds = TimeUnit.SECONDS.toSeconds(secondsNum) - (TimeUnit.SECONDS.toMinutes(secondsNum) *60);
        if (seconds < 10) {
            return "0" + seconds;
        } else {
            return String.valueOf(seconds);
        }
    }

    private void writeFinalRecognizerResult(Recognizer recognizer, CacheInfo cacheInfo) {
        String finalJsonResult = recognizer.getFinalResult();
        writeToCacheFiles(finalJsonResult, cacheInfo);
    }
}
