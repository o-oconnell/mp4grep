package Search;

import SpeechToText.SpeechToText;

import java.io.File;

public class GrepCacheWrapper {

    private static final String CACHE_DIRECTORY = ".cache";
    private static final String TRANSCRIPT_FILE_EXTENSION = ".transcript";
    private static final String TIMESTAMP_FILE_EXTENSION = ".timestamp";
    SpeechToText speechToText;

    public GrepCacheWrapper(SpeechToText speechToText) {
        this.speechToText = speechToText;
    }

    public void search(String file, String searchString) {

        CacheKey cacheKey = new CacheKey(file, speechToText);

        if (cachedTranscriptExists(cacheKey)) {
            Greppable grep = getGreppableFromCacheKey(cacheKey);
            grep.search(searchString);
        } else {
            Greppable grep = speechToText.getGreppableResult(cacheKey);
            grep.search(searchString);

            // TODO: This is janky. Get the sound engine to use the output formatter directly and print to the correct location.
            grep.storeTranscriptInLocation(getTranscriptFile(cacheKey));
            grep.storeTimestampsInLocation(getTimestampFile(cacheKey));
        }
    }

    private boolean cachedTranscriptExists(CacheKey cacheKey) {

        File transcriptFile = getTranscriptFile(cacheKey);
        File timestampFile = getTimestampFile(cacheKey);

        if (transcriptFile.exists() && timestampFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private File getTranscriptFile(CacheKey cacheKey) {
        String hash = getHash(cacheKey);
        return new File(getCachedFileLocation(hash + TRANSCRIPT_FILE_EXTENSION));
    }

    private File getTimestampFile(CacheKey cacheKey) {
        String hash = getHash(cacheKey);
        return new File(getCachedFileLocation(hash + TIMESTAMP_FILE_EXTENSION));
    }

    private String getCachedFileLocation(String filename) {
        return CACHE_DIRECTORY + "/" + filename;
    }

    private Greppable getGreppableFromCacheKey(CacheKey cacheKey) {
        File timestamps = getTimestampFile(cacheKey);
        File transcript = getTranscriptFile(cacheKey);

        String timestampFile = timestamps.toString();
        String transcriptFile = transcript.toString();

        return new GreppableTranscript(timestampFile, transcriptFile, 100, 100);
    }

    public static String getHash(CacheKey cacheKey) {
        Object speechToText = cacheKey.speechToText;
        String speechToTextName = speechToText.getClass().getName();
        String preHashString = cacheKey.filename + cacheKey.lastModified + speechToTextName;

        return String.valueOf(preHashString.hashCode());
    }
}
