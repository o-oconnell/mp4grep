package Transcribe;

import Search.Searchable;
import Transcribe.Cache.CacheInfo;

public class VoskProxy {
    public VoskAdapter voskAdapter;

    public VoskProxy() {
        this.voskAdapter = new VoskAdapter();
    }

    public Searchable getSearchableTranscript(CacheInfo cacheInfo) {
        voskAdapter.transcribeAudio(cacheInfo);
        return new Searchable(cacheInfo);
    }
}
