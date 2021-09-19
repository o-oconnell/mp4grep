package Transcribe;

import Arguments.TranscriptArguments;
import Search.Searchable;
import Transcribe.Cache.TranscriptCache;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TranscriptAdapter {
    private List<String> files;
    private VoskAdapter speechToText;

    public TranscriptAdapter(TranscriptArguments transcriptArguments) {
        this.files = transcriptArguments.files;
        this.speechToText = transcriptArguments.speechToText;
    }

    public List<Searchable> getSearchables() {
        if (files.isEmpty()) {
            return new LinkedList<Searchable>();
        }

        List<Searchable> result = files.parallelStream()
                    .map(this::callCacheForInput)
                    .collect(Collectors.toList());

        return result;
    }

    private Searchable callCacheForInput(String filename) {
        TranscriptCache cache = new TranscriptCache(filename, speechToText);
        return cache.getSearchable();
    }
}
