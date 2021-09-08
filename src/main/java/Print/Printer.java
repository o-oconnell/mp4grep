package Print;

import Arguments.PrintArguments;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Printer {
    private static final int PERIOD_MATCHES_NEWLINES_AND_SPACES = Pattern.DOTALL;
    private static final String WORD_DELIMITER = " ";

    private PrintArguments args;

    public Printer(PrintArguments args) {
        this.args = args;
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
            IntegerPair timestampPair = printable.transcriptTimestampIndices.get(timestampKey);
            printResult(printable, timestampPair, printPair);
        }
    }

    private int getTimestampKey(Printable printable, int index) {
        return printable.transcriptTimestampIndices.floorKey(index);
    }

    private void printResult(Printable printable, IntegerPair timestampPair, IntegerPair printPair) {
        System.out.print(stripSpaces(printable.timestamps.substring(timestampPair.start, timestampPair.end + 1)) + " ");
        System.out.println(stripSpaces(printable.transcript.substring(printPair.start, printPair.end + 1)));
    }

    private void printFilename(Printable printable) {
        System.out.println("");
        System.out.println("File: " +printable.filename);
        System.out.println("--------");
    }

    private int getPrintStart(int matchStart, String transcript) {
        int printStart = getPreviousDelimiterIndex(matchStart, transcript);
        for (int i = 0; i < args.wordsBeforeMatch; ++i) {
            int prevIndex = getPreviousDelimiterIndex(printStart, transcript);
            if (prevIndex < matchStart && prevIndex >= 0) {
                printStart = prevIndex;
            }
            if (prevIndex == -1) { // we have reached the beginning of the string
                printStart = 0;
            }
        }
        return printStart;
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        return transcript.lastIndexOf(WORD_DELIMITER, currentIndex - 1);
    }

    private int getPrintEnd(int matchEnd, String transcript) {
        int printEnd = matchEnd;
        for (int i = 0; i < args.wordsAfterMatch; ++i) {
            int nextIndex = getNextDelimiterIndex(printEnd, transcript);
            if (nextIndex > printEnd && nextIndex < transcript.length()) {
                printEnd = nextIndex;
            }
        }
        return printEnd;
    }

    private int getNextDelimiterIndex(int currentIndex, String transcript) {
        return transcript.indexOf(WORD_DELIMITER, currentIndex + 1);
    }

    private String stripSpaces(String input) {
        return input.trim();
    }
}
