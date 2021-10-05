package Transcribe;


import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;

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
        String result = getFilenameWithoutExtension(file) + "." + VOSK_AUDIO_FILE_FORMAT;
        return CONVERTED_AUDIO_FILE_DIRECTORY + result;
    }

    private static String getFilenameWithoutExtension(String file) {
        int extensionStartIndex = file.lastIndexOf(".") + 1;
        return file.substring(0, extensionStartIndex - 1);
    }

    private static void convertAndWriteToTargetFile(String sourceFilename, String targetFilename) {
        createConversionDirectory();
        EncodingAttributes attributes = getEncodingAttributesForWAVWithAudioAttributes();
        encodeAudio(sourceFilename, targetFilename, attributes);
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

    private static void encodeAudio(String sourceFilename, String targetFilename, EncodingAttributes attributes) {
        File source = new File(sourceFilename);
        File target = new File(targetFilename);
        MultimediaObject instance = new MultimediaObject(source);
        Encoder encoder = new Encoder();

        try {
            encoder.encode(instance, target, attributes);
        } catch (EncoderException e) {
            System.out.println("Error converting input file " + sourceFilename + " to VOSK-compatible WAV format.");
            e.printStackTrace();
        }
    }

    public static long getAudioDuration(String sourceFilename) {
        File source = new File(sourceFilename);
        MultimediaObject instance = new MultimediaObject(source);

        MultimediaInfo info = null;
        try {
            info = instance.getInfo();
        } catch (EncoderException e) {
            System.out.println("Error finding audio duration");
            e.printStackTrace();
        }
        return info.getDuration();
    }

    public static String getConvertedFilename(String file) {
        String result = getFilenameWithoutExtension(file) + "." + VOSK_AUDIO_FILE_FORMAT;
        return CONVERTED_AUDIO_FILE_DIRECTORY + result;
    }
}
