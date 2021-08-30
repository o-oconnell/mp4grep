package Search;

import Arguments.GrepperArguments;
import Arguments.SpeechToTextArguments;
import Output.MultithreadedPrinter;
import SpeechToText.SpeechToText;
import SpeechToText.VoskSpeechToText;

import java.io.File;

public class GrepCacheWrapper {

    private static final String CACHE_DIRECTORY = ".cache";
    private static final String TRANSCRIPT_FILE_EXTENSION = ".transcript";
    private static final String TIMESTAMP_FILE_EXTENSION = ".timestamp";

    private GrepperArguments arguments;
    private SpeechToText speechToText;
    private SpeechToTextArguments speechToTextArguments;

    private int wordsToPrintAfterMatch;
    private int wordsToPrintBeforeMatch;

    public GrepCacheWrapper(GrepperArguments arguments) {
        this.arguments = arguments;
        // this.speechToText = arguments.speechToText;
        this.speechToTextArguments = arguments.speechToTextArguments;
        this.wordsToPrintAfterMatch = speechToTextArguments.wordsToPrintAfterMatch;
        this.wordsToPrintBeforeMatch = speechToTextArguments.wordsToPrintBeforeMatch;
    }

    public void search(String file, String searchString) {
        this.speechToText = new VoskSpeechToText();
        CacheKey cacheKey = new CacheKey(file, speechToText);
        System.out.println("Creating new cache key for file: " + file);
        Greppable grep = getGreppable(cacheKey);
        grep.search(searchString);
    }

    private Greppable getGreppable(CacheKey cacheKey) {
        if (cacheKey.cachedFilesExist()) {
            return getGreppableFromCacheKey(cacheKey);
        } else {
            return speechToText.getGreppableResult(cacheKey, speechToTextArguments);
        }
    }

    private Greppable getGreppableFromCacheKey(CacheKey cacheKey) {
        return new GreppableTranscript(cacheKey, wordsToPrintBeforeMatch, wordsToPrintAfterMatch);
    }
}
