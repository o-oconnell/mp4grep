package Transcription;

import java.Search.Searchable;

public class VoskProxy {

    public VoskProxy() {}

    public Searchable searchUsingVosk(CacheKey cacheKey) {
        VoskAdapter voskAdapter = new VoskAdapter();
        return voskAdapter.getSearchableResult(cacheKey);
    }
}
