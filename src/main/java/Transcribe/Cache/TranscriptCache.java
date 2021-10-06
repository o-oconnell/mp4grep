package Transcribe.Cache;

import Search.Searchable;
import Transcribe.Cache.CacheInfo;
import Transcribe.Cache.CacheKey;
import Transcribe.VoskAdapter;
import Transcribe.VoskConverter;
import Transcribe.VoskProxy;
import org.apache.commons.io.FileUtils;

import static Globals.GlobalLocations.CACHE_DIRECTORY;

import java.io.File;
import java.io.IOException;

public class TranscriptCache {
    private String filename;
    public VoskProxy voskProxy;
    private String modelDirectory;
    private CacheInfo cacheInfo;
    private boolean needsTranscribing;

    public TranscriptCache(String filename, String modelDirectory) {
        this.filename = filename;
        this.modelDirectory = modelDirectory;
        this.voskProxy = new VoskProxy();
        this.cacheInfo = getCacheInfo(new CacheKey(filename, modelDirectory));
        this.needsTranscribing = !cachedFilesExist(cacheInfo);
    }

    public boolean needsTranscribing() {
        return needsTranscribing;
    }

    public String getFilename() {
        return cacheInfo.inputFilename;
    }

    public long getFileDurationMillis() {
        return VoskConverter.getAudioDuration(cacheInfo.inputFilename);
    }

    public Searchable getSearchable() {

        if (needsTranscribing) {
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

    public static void clearCacheFiles() {
        File cacheDirectory = new File(CACHE_DIRECTORY);
        try {
            FileUtils.cleanDirectory(cacheDirectory);
        } catch (IOException e) {
            System.out.println("Failed to clear the cache directory: " + cacheDirectory.getAbsolutePath());
        }
    }
}

