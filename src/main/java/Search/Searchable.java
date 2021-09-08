package java.Search;

import Transcription.CacheKey;

import java.io.File;

public class Searchable {
    public File timestampFile;
    public File transcriptFile;
    public String filename;

    public Searchable(CacheKey cacheKey) {
        this.timestampFile = cacheKey.getTimestampFile();
        this.transcriptFile = cacheKey.getTranscriptFile();
        this.filename = cacheKey.filename;
    }
}
