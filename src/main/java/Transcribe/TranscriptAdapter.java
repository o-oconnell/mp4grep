package Transcribe;

import Arguments.TranscriptArguments;
import Search.Searchable;
import Transcribe.Cache.TranscriptCache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static Globals.GlobalColors.*;

public class TranscriptAdapter {
    private static final int PROGRESS_BAR_CONTENT_WIDTH = 44;
    private static final String PRINT_STRING_FORMAT = "Transcribing audio files: %.2f%% %s\r";

    private final List<String> files;
    private final String modelDirectory;

    private List<Searchable> searchables;
    private AtomicBoolean transcriptionInProgress;
    private long totalTranscribeDurationMillis;

    public TranscriptAdapter(TranscriptArguments transcriptArguments) {
        this.files = transcriptArguments.files;
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
                if (totalTranscribeDurationMillis == 0) {
                    break; // All files must have been transcribed already.
                }

                double sum = 0;
                for (TranscriptCache cache : caches) {
                    if (cache.needsTranscribing()) {
                        sum = sum + cache.voskProxy.voskAdapter.progress;
                    }
                }
                printProgress(sum);
                sleepOneMinute();
            }

            printProgress(totalTranscribeDurationMillis);
        });
    }

    private void printProgress(double progressSumMillis) {
        double percent = -1;
        if (totalTranscribeDurationMillis == 0)
            percent = 100; // In this case, we do not have any files to transcribe.
        else
            percent = progressSumMillis / totalTranscribeDurationMillis * 100;

        String percentageString = String.format(PRINT_STRING_FORMAT, percent, getProgressBar(percent));
        System.out.print(percentageString);
    }

    private String getProgressBar(double percent) {
        int truncatedPercent = (int) percent;

        String progress = "|" + ANSI_YELLOW;
        for (int i = 0; i < PROGRESS_BAR_CONTENT_WIDTH; i++) {
            int currentPercent = (int) (100 * (double) i / PROGRESS_BAR_CONTENT_WIDTH);

            if (currentPercent <= truncatedPercent)
                progress += "#";
            else
                progress += " ";
        }
        progress += ANSI_RESET + "|";

        return progress;
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
