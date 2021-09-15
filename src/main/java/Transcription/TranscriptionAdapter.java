package Transcription;

import Arguments.TranscriptArguments;
import Search.Searchable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TranscriptionAdapter {
    private List<String> files;
    private VoskAdapter speechToText;

    public TranscriptionAdapter(TranscriptArguments transcriptArguments) {
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
        TranscriptionCache cache = new TranscriptionCache(filename, speechToText);
        return cache.getSearchable();
    }
}
