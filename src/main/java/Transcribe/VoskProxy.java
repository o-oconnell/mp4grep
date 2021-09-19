package Transcribe;

import Search.Searchable;
import Transcribe.Cache.CacheInfo;

public class VoskProxy {

    public VoskProxy() {}

    public Searchable getSearchableTranscript(CacheInfo cacheInfo) {
        VoskAdapter voskAdapter = new VoskAdapter();
        voskAdapter.transcribeAudio(cacheInfo);
        return new Searchable(cacheInfo);
    }

}
