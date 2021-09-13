package Transcription;

import java.io.File;

public class CacheKey {
    private static final String CACHE_DIRECTORY = ".cache";
    private static final String TRANSCRIPT_FILE_EXTENSION = ".transcript";
    private static final String TIMESTAMP_FILE_EXTENSION = ".timestamp";

    public String filename;
    public String lastModified;
    public VoskAdapter speechToText;
    private String hash;

    public CacheKey(String filename, VoskAdapter speechToText) {
        this.filename = filename;
        this.lastModified = getLastModified(filename);
        this.speechToText = speechToText;
        this.hash = computeHash();
    }

    private String getLastModified(String filename) {
        File file = new File(filename);
        return String.valueOf(file.lastModified());
    }

    private String computeHash() {
        Object speechToTextObject = speechToText;
        String speechToTextName = speechToTextObject.getClass().getName();
        String preHashString = filename + lastModified + speechToTextName;

        return String.valueOf(preHashString.hashCode());
    }

    public String getTranscriptFilename() {
        return CACHE_DIRECTORY + "/" + this.hash + TRANSCRIPT_FILE_EXTENSION;
    }

    public String getTimestampFilename() {
        return CACHE_DIRECTORY + "/" + this.hash + TIMESTAMP_FILE_EXTENSION;
    }

    public File getTranscriptFile() {
        return new File(getTranscriptFilename());
    }

    public File getTimestampFile() {
        return new File(getTimestampFilename());
    }

}
