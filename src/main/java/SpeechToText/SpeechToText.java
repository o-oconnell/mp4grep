package SpeechToText;

import Arguments.SpeechToTextArguments;
import Search.CacheKey;
import Search.Greppable;

public interface SpeechToText {

    Greppable getGreppableResult(CacheKey cacheKey, SpeechToTextArguments arguments);


}
