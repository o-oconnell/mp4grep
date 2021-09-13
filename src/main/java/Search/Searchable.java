package Search;

import Transcription.CacheInfo;
import Transcription.CacheKey;

import java.io.File;

public class Searchable {
    public File timestampFile;
    public File transcriptFile;
    public String filename;

    public Searchable(CacheInfo cacheInfo) {
        this.timestampFile = new File(cacheInfo.timestampFilename);
        this.transcriptFile = new File(cacheInfo.transcriptFilename);
        this.filename = cacheInfo.inputFilename;
    }
}
