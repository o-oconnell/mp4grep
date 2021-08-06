package Output;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.util.ArrayList;

public class SpeechToTextOutput {

    private class TimestampedResult {
        private double start;
        private double end;
        private String content;

        public TimestampedResult(double start, double end, String content) {
            this.start = start;
            this.end = end;
            this.content = content;
        }

        public String toString() {
            return String.format(start + "---" + end + ": " + content);
        }

        public double getDuration() {
            return end - start;
        }

        public void addResult(TimestampedResult other) {
            String newContent = this.content + " " + other.content;
            this.content = newContent;
            this.end = other.end;
        }
    }

    private int maxTimestampTimeSeconds;
    private ArrayList<TimestampedResult> output;

    public SpeechToTextOutput(int maxTimestampTimeSeconds) {
        this.maxTimestampTimeSeconds = maxTimestampTimeSeconds;
        output = new ArrayList<TimestampedResult>();
    }

    public void addTimestampedWordToOutput(double timestampStart, double timestampEnd, String content) {
        TimestampedResult result = new TimestampedResult(timestampStart, timestampEnd, content);

        if (!outputIsEmpty() && sumDurations(getLastResult(), result) < maxTimestampTimeSeconds) {
            combineCurrentTimestampedStringIntoPrevious(result);
        } else {
            output.add(result);
        }
    }

    private boolean outputIsEmpty() {
        return output.isEmpty();
    }

    private double sumDurations(TimestampedResult a, TimestampedResult b) {
        return a.getDuration() + b.getDuration();
    }

    private void combineCurrentTimestampedStringIntoPrevious(TimestampedResult result) {
        TimestampedResult lastResult = getLastResult();
        lastResult.addResult(result);
    }

    private TimestampedResult getLastResult() {
        return output.get(output.size() - 1);
    }

    public void print() {
        for (TimestampedResult result : output) {
            System.out.println(result.toString());
        }
    }

    public ArrayList<String> toStringArrayList() {
        ArrayList<String> result = new ArrayList<String>();
        for (TimestampedResult r : output) {
            result.add(r.toString());
        }
        return result;
    }

    public void printToFile(String filename) {
        for (TimestampedResult result : output) {
            String content = result.toString();
            appendToFile(filename, content);
        }
    }

    private void appendToFile(String filename, String content) {
        Path outputFile = Path.of(filename);

        try {
            Files.writeString(outputFile, content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error printing formatted output to file. Stacktrace:");
            e.printStackTrace();
        }
    }

}
