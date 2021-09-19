package Transcribe.Cache;

import lombok.Builder;

@Builder
public class CacheInfo {
    public String inputFilename;
    public String transcriptFilename;
    public String timestampFilename;
}
