package Transcribe;

import Arguments.TranscriptArguments;
import Search.Searchable;
import Transcribe.Cache.TranscriptCache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TranscriptAdapter {
    private List<String> files;
    private VoskAdapter speechToText;
    private String modelDirectory;
    private List<Searchable> searchables;
    private AtomicBoolean transcriptionInProgress;
    private long totalTranscribeDurationMillis;


    public TranscriptAdapter(TranscriptArguments transcriptArguments) {
        this.files = transcriptArguments.files;
        this.speechToText = transcriptArguments.speechToText;
        this.modelDirectory = transcriptArguments.modelDirectory;
    }

    public List<Searchable> getSearchables() {
        if (files.isEmpty()) {
            return new LinkedList<>();
        }

        List<TranscriptCache> caches = getCaches();
        setTotalTranscribeDuration(caches);
        Thread progressBar = getProgressBar(caches);
        Thread transcription = getTranscription(caches);

        startThreads(progressBar, transcription);
        joinThreads(progressBar, transcription);

        return searchables;
    }

    private void startThreads(Thread progressBar, Thread transcription) {
        this.transcriptionInProgress = new AtomicBoolean(true);
        progressBar.start();
        transcription.start();
    }

    private void joinThreads(Thread progressBar, Thread transcription) {
        try {
            progressBar.join();
            transcription.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sleepOneMinute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private List<TranscriptCache> getCaches() {
        List<TranscriptCache> caches = new ArrayList<>();
        for (int i = 0; i < files.size(); ++i) {
            caches.add(new TranscriptCache(files.get(i), modelDirectory));
        }
        return caches;
    }

    private void setTotalTranscribeDuration(List<TranscriptCache> caches) {
        for (TranscriptCache cache : caches) {
            if (cache.needsTranscribing()) {
                totalTranscribeDurationMillis += cache.getFileDurationMillis();
            }
        }
    }

    private Thread getProgressBar(List<TranscriptCache> caches) {
        return new Thread(() -> {
            while (transcriptionInProgress.get() == true) {
                double sum = 0;
                for (TranscriptCache cache : caches) {
                    if (cache.needsTranscribing()) {
                        sum = sum + cache.voskProxy.voskAdapter.progress;
                    }
                }
                String percentage = String.format("%.2f", sum / totalTranscribeDurationMillis * 100);
                if (totalTranscribeDurationMillis > 0) // in the case that all files have been transcribed already
                    System.out.print("Transcribing audio files: " +  percentage + "%\r");
                sleepOneMinute();
            }
            System.out.print("Transcribing audio files: " + 100.0 + "%");
        });
    }

    private Thread getTranscription(List<TranscriptCache> caches) {
       return new Thread(() -> {
            searchables = caches.parallelStream()
                    .map(TranscriptCache::getSearchable)
                    .collect(Collectors.toList());
            transcriptionInProgress.set(false);
        });
    }
}
