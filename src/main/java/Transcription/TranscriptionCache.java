package Transcription;

import Search.Searchable;

import java.io.File;

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

        if (cachedFilesExist(cacheKey)) {
            return new Searchable(cacheKey);
        }
        return voskProxy.transcribeWithVosk(getVoskAdapterArguments(cacheKey));
    }

    private boolean cachedFilesExist(CacheKey cacheKey) {
        File transcript = cacheKey.getTranscriptFile();
        File timestamps = cacheKey.getTimestampFile();
        return transcript.exists() && timestamps.exists();
    }

    private CacheInfo getVoskAdapterArguments(CacheKey cacheKey) {
        return CacheInfo
                .builder()
                .inputFilename(cacheKey.filename)
                .timestampFilename(cacheKey.getTimestampFilename())
                .transcriptFilename(cacheKey.getTranscriptFilename())
                .build();
    }
}

