package Transcription;

import Search.Searchable;

public class VoskProxy {

    public VoskProxy() {}

    public Searchable getSearchableTranscript(CacheInfo cacheInfo) {
        VoskAdapter voskAdapter = new VoskAdapter();
        voskAdapter.transcribeAudio(cacheInfo);
        return new Searchable(cacheInfo);
    }

}
