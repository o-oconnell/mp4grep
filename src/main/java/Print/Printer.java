package Print;

import Arguments.PrintArguments;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class Printer {
    private static final char DELIMITER = ' ';
    private PrintArguments printArguments;

    public Printer(PrintArguments printArguments) {
        this.printArguments = printArguments;
    }

    public void print(Printable printable) {
        List<IntegerPair> printIndices = getPrintIndices(printable);
        printFilename(printable);
        printAll(printIndices, printable);
    }

    List<IntegerPair> getPrintIndices(Printable printable) {
        return printable.matchIndices
                .stream()
                .map(match -> getPrintPair(match, printable.transcript))
                .collect(Collectors.toList());
    }

    IntegerPair getPrintPair(IntegerPair match, String transcript) {
        int start = getPrintStart(match.start, transcript);
        int end = getPrintEnd(match.end, transcript);
        return new IntegerPair(start, end);
    }

    private void printAll(List<IntegerPair> printIndices, Printable printable) {
        for (IntegerPair printPair : printIndices) {
            int timestampKey = getTimestampKey(printable, printPair.start);
            String timestamp = printable.transcriptTimestamps.get(timestampKey);
            printResult(printable, timestamp, printPair);
        }
    }

    private int getTimestampKey(Printable printable, int index) {
        return printable.transcriptTimestamps.floorKey(index);
    }

    private void printResult(Printable printable, String timestamp, IntegerPair printPair) {
        System.out.print(timestamp);
        System.out.print(":");
        printMatch(printPair, printable.transcript);
    }

    private void printMatch(IntegerPair printPair, String transcript) {
        String substr = transcript.substring(printPair.start, printPair.end + 1);
        System.out.println(substr);
    }

    private void printFilename(Printable printable) {
        System.out.println("");
        System.out.println("File: " +printable.filename);
        System.out.println("--------");
    }

    private int getPrintStart(int matchStart, String transcript) {
        int printStart = getPreviousDelimiterIndex(matchStart, transcript);
        for (int i = 0; i < printArguments.wordsBeforeMatch; ++i) {
            int prevIndex = getPreviousDelimiterIndex(printStart, transcript);
            if (prevIndex < matchStart && prevIndex >= 0) {
                printStart = prevIndex;
            }
            if (prevIndex == -1) { // we have reached the beginning of the string
                printStart = 0;
            }
        }
        return stripLeadingDelimiters(printStart, transcript);
    }

    private int stripLeadingDelimiters(int start, String transcript) {
        int newIndex = start;
        while (transcript.charAt(newIndex) == DELIMITER && newIndex < transcript.length() - 1) {
            newIndex++;
        }
        return newIndex;
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        int prevIndex = transcript.lastIndexOf(DELIMITER, currentIndex - 1);
        if (prevIndex < 0) {
            return 0;
        } else {
            return prevIndex;
        }
    }

    private int getPrintEnd(int matchEnd, String transcript) {
        int printEnd = matchEnd;
        for (int i = 0; i < printArguments.wordsAfterMatch; ++i) {
            int nextIndex = getNextDelimiterIndex(printEnd, transcript);
            if (nextIndex > printEnd && nextIndex < transcript.length()) {
                printEnd = nextIndex;
            }
        }
        return stripTrailingDelimiters(printEnd, transcript);
    }

    private int stripTrailingDelimiters(int end, String transcript) {
        int newIndex = end;
        while (transcript.charAt(newIndex) == DELIMITER && newIndex > 0) {
            newIndex--;
        }
        return newIndex;
    }

    private int getNextDelimiterIndex(int currentIndex, String transcript) {
        return transcript.indexOf(DELIMITER, currentIndex + 1);
    }

    private String stripSpaces(String input) {
        return input.trim();
    }
}
