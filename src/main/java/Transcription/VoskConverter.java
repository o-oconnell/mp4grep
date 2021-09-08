package Transcription;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VoskConverter {

    private static final int VOSK_ACCEPTED_SAMPLING_RATE = 16000;
    private static final int VOSK_NUMBER_ACCEPTED_CHANNELS = 1;
    private static final String VOSK_AUDIO_FILE_FORMAT = "wav";
    private static final String VOSK_AUDIO_CODEC = "pcm_s16le";
    private static final String CONVERTED_AUDIO_FILE_DIRECTORY = ".converted";

    public static String convertToVoskFormat(String sourceFile) {
        createConversionDirectory();
        String targetFile = makeTargetFilename(sourceFile);
        convertAndWriteToTargetFile(sourceFile, targetFile);
        return targetFile;
    }

    private static void createConversionDirectory() {
        File conversionDirectory = new File(CONVERTED_AUDIO_FILE_DIRECTORY);
        if (!conversionDirectory.exists()) {
            createNewConversionDirectory();
        }
    }

    private static void createNewConversionDirectory() {
        Path conversionDirPath = Path.of(CONVERTED_AUDIO_FILE_DIRECTORY);
        try {
            Files.createDirectory(conversionDirPath);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static String makeTargetFilename(String file) {
        int extensionStartIndex = file.lastIndexOf(".") + 1;
        String nameWithoutExtension = file.substring(0, extensionStartIndex - 1);
        String result = nameWithoutExtension + "." + VOSK_AUDIO_FILE_FORMAT;
        return CONVERTED_AUDIO_FILE_DIRECTORY + "/" + result;
    }

    private static void convertAndWriteToTargetFile(String sourceFilename, String targetFilename) {
        File source = new File(sourceFilename);
        File target = new File(targetFilename);
        EncodingAttributes attributes = getEncodingAttributesForWAVWithAudioAttributes();
        encodeToTargetFile(source, target, attributes);
    }

    private static EncodingAttributes getEncodingAttributesForWAVWithAudioAttributes() {
        AudioAttributes audio = getAudioAttributesForVosk();
        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setOutputFormat(VOSK_AUDIO_FILE_FORMAT);
        attributes.setAudioAttributes(audio);
        return attributes;
    }

    private static AudioAttributes getAudioAttributesForVosk() {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(VOSK_AUDIO_CODEC);
        audio.setChannels(VOSK_NUMBER_ACCEPTED_CHANNELS);
        audio.setSamplingRate(VOSK_ACCEPTED_SAMPLING_RATE);
        return audio;
    }

    private static void encodeToTargetFile(File source, File target, EncodingAttributes attributes) {
        MultimediaObject instance = new MultimediaObject(source);
        Encoder encoder = new Encoder();

        try {
            encoder.encode(instance, target, attributes);
        } catch (EncoderException e) {
            System.out.println("Error converting files file to VOSK-compatible WAV format.");
            e.printStackTrace();
        }
    }
}
