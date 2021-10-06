package Transcribe;

import Arguments.TranscriptArguments;
import Search.Searchable;
import Transcribe.Cache.TranscriptCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TranscriptAdapter {
    private static int MIN_PROGRESS_BAR_LENGTH = 10;
    private static String PERCENT_COMPLETION_FORMAT = "Transcribing audio files: %.2f% %s\r"; // [percentage] [progress bar]

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
            System.out.println(""); // Go to the next line so this line is not overwritten by the printing of the grep output.
        });
    }

    private void printProgress(double progressSumMillis) {
        double percent = -1;
        if (totalTranscribeDurationMillis == 0)
            percent = 100;
        else
            percent = progressSumMillis / totalTranscribeDurationMillis * 100;

        String percentageString = String.format("Transcribing audio files: %.2f%%\r", percent);
        System.out.print(percentageString);
    }

    private String getProgressBar(double percentNum) {
        String printStr = "Transcribing audio files: 100.0% \r";

        int terminalWidth = getTerminalWidth();
        int progressBarWidth = terminalWidth - printStr.length();

        if (progressBarWidth <= 10) {
            return "";
        }

        int percent = (int) percentNum;

        String progress = "";
        for (int i = 0; i < MIN_PROGRESS_BAR_LENGTH; i++) {
            if (100 * (i / progressBarWidth) <= percent)
                progress += "#";
            else
                progress += " ";
        }

        return progress;
    }

    private int getTerminalWidth() {
        int terminalWidth = -1;
        try {
            terminalWidth = org.jline.terminal.TerminalBuilder.terminal().getWidth();
        } catch (IOException e) {
            System.out.println("Error reading terminal width");
            e.printStackTrace();
        }
        return terminalWidth;
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
