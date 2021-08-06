package Search;

import SpeechToText.SpeechToText;

import java.io.File;

public class CacheKey {
    public String filename;
    public String lastModified;
    public SpeechToText speechToText;

    public CacheKey(String filename, SpeechToText speechToText) {
        this.filename = filename;
        this.lastModified = getLastModified(filename);
        this.speechToText = speechToText;
    }

    private String getLastModified(String filename) {
        File file = new File(filename);
        return String.valueOf(file.lastModified());
    }
}
