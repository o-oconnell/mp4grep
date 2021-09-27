package Transcribe;

import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class ParallelProgressManager {
    public static class Progress {
        public String name;
        public int progressIndex;
        public long maxValue;
        public long completion;

        public Progress(String name, int progressIndex, int maxValue) {
            this.name = name;
            this.progressIndex = progressIndex;
            this.maxValue = maxValue;
        }

        public void setCompletion(long completion) {
            this.completion = completion;
        }

        public void setCompleted() {
            this.completion = this.maxValue;
        }

        public boolean isCompleted() {
            return getCompletionPercent() == 100;
        }

        public int getCompletionPercent() {
            return (int) (completion / maxValue) * 100;
        }
    }

    public static int MAX_PROGRESS_PERCENT = 100;
    public static List<Progress> progressList = new ArrayList<>();
    public static List<ProgressBar> progressBars = new ArrayList<>();

    public static Progress getNewProgress(String name, int maxValue) {
        Progress progress = new Progress(name, progressList.size(), maxValue);
        progressList.add(progress);
        progressBars.add(new ProgressBar(name, MAX_PROGRESS_PERCENT));
        return progress;
    }

    public void updateProgress(Progress progress, long value) {
        progressList.get(progress.progressIndex);
        progressBars.get(progress.progressIndex).stepTo(progress.getCompletionPercent());
    }
}
