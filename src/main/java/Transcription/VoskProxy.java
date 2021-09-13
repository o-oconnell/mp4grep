package Transcription;

import Search.Searchable;

public class VoskProxy {

    public VoskProxy() {}

    public Searchable transcribeWithVosk(CacheKey cacheKey) {
        VoskAdapter voskAdapter = new VoskAdapter();
        voskAdapter.transcribeAudio(getVoskAdapterArguments(v));
        return new Searchable(cacheKey);
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
