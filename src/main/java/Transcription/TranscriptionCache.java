package Transcription;

import Search.Searchable;

public class TranscriptionCache {
    private VoskAdapter speechToText;
    private String filename;
    private VoskProxy voskProxy;

    public TranscriptionCache(String filename, VoskAdapter speechToText) {
        this.filename = filename;
        this.speechToText = speechToText;
        this.voskProxy = new VoskProxy();
    }

    public Searchable getSearchable() {
        CacheKey cacheKey = new CacheKey(filename, speechToText);

        if (!cacheKey.cachedFilesExist()) {
            return voskProxy.transcribeWithVosk(cacheKey);
        }
        return new Searchable(cacheKey);
    }
}

