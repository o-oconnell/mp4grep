package Transcription;

import Search.Searchable;

public class VoskProxy {

    public VoskProxy() {}

    public Searchable transcribeWithVosk(CacheKey cacheKey) {
        VoskAdapter voskAdapter = new VoskAdapter();
        voskAdapter.transcribeAudio(cacheKey);
        return new Searchable(cacheKey);
    }
}
