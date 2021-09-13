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
        CacheInfo cacheInfo = getCacheInfo(new CacheKey(filename, speechToText));

        if (!cachedFilesExist(cacheInfo)) {
            voskProxy.getSearchableTranscript(cacheInfo);
        }
        return new Searchable(cacheInfo);
    }

    private boolean cachedFilesExist(CacheInfo cacheInfo) {
        File transcript = new File(cacheInfo.transcriptFilename);
        File timestamps = new File(cacheInfo.timestampFilename);
        return transcript.exists() && timestamps.exists();
    }

    private CacheInfo getCacheInfo(CacheKey cacheKey) {
        return CacheInfo
                .builder()
                .inputFilename(cacheKey.filename)
                .timestampFilename(cacheKey.getTimestampFilename())
                .transcriptFilename(cacheKey.getTranscriptFilename())
                .build();
    }
}

