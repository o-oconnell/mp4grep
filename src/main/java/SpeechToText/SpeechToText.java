package SpeechToText;

import Search.CacheKey;
import Search.Greppable;

public interface SpeechToText {

    Greppable getGreppableResult(CacheKey cacheKey);


}
