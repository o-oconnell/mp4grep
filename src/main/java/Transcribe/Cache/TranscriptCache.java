package Transcribe.Cache;

import Search.Searchable;
import Transcribe.Cache.CacheInfo;
import Transcribe.Cache.CacheKey;
import Transcribe.VoskAdapter;
import Transcribe.VoskProxy;

import java.io.File;

public class TranscriptCache {
    private VoskAdapter speechToText;
    private String filename;
    private VoskProxy voskProxy;
    private String modelDirectory;

    public TranscriptCache(String filename, VoskAdapter speechToText, String modelDirectory) {
        this.filename = filename;
        this.speechToText = speechToText;
        this.modelDirectory = modelDirectory;
        this.voskProxy = new VoskProxy();
    }

    public Searchable getSearchable() {
        CacheInfo cacheInfo = getCacheInfo(new CacheKey(filename, speechToText, modelDirectory));

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
                .modelDirectory(cacheKey.getModelDirectory())
                .build();
    }
}

