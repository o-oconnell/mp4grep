package Print;

import Arguments.PrintArguments;
import Search.Searcher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Printer {
    private static final char DELIMITER = ' ';
    private static final String ANSI_RED_MATCH_HIGHLIGHT = "\u001B[31m";
    private static final String ANSI_GREEN_FILENAME = "\u001b[32m";
    private static final String ANSI_BLUE_TIMESTAMP = "\u001b[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    private PrintArguments printArguments;

    public Printer(PrintArguments printArguments) {
        this.printArguments = printArguments;
    }

    public void print(Printable printable) {
        printFilename(printable);
        printMatches(printable);
    }

    private void printFilename(Printable printable) {
        System.out.println();
        System.out.println(ANSI_GREEN_FILENAME + printable.filename + ANSI_RESET);
    }

    private void printMatches(Printable printable) {
        List<Integer> delimiterIndices = getDelimiterIndices(printable.transcript);
        ProcessedTranscript processedTranscript = new ProcessedTranscript(printable.transcript, delimiterIndices);
        ProcessedTimestamps processedTimestamps = new ProcessedTimestamps(printable.timestamps, delimiterIndices);

        printable.matchIndices.stream()
                .forEach(integerPair -> printMatch(integerPair, processedTranscript, processedTimestamps));
    }

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length() - 1)
                .boxed()
                .filter(i -> string.charAt(i) == DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private void printMatch(IntegerPair matchPair, ProcessedTranscript processedTranscript, ProcessedTimestamps processedTimestamps) {
        System.out.println(
                ANSI_BLUE_TIMESTAMP + "[" + getTimestampPrint(matchPair, processedTimestamps) + "] " + ANSI_RESET
                + getTranscriptPrint(matchPair, processedTranscript));
    }

    public String getTimestampPrint(IntegerPair matchPair, ProcessedTimestamps processed) {
        int index = processed.transcriptTimestampMap.floorKey(matchPair.start);
        String timestamp = processed.transcriptTimestampMap.get(index);

        for (int i = 0; i < printArguments.wordsBeforeMatch; ++i) {
            Integer prevKey = processed.transcriptTimestampMap.lowerKey(index);
            if (prevKey != null) {
                index = prevKey;
                timestamp = processed.transcriptTimestampMap.get(prevKey);
            }
        }
        return timestamp;
    }

    public String getTranscriptPrint(IntegerPair matchPair, ProcessedTranscript processed) {
        int startPrevDelimiter = getPreviousDelimiterIndex(matchPair.start, processed.transcript);
        int endPrevDelimiter = getPreviousDelimiterIndex(matchPair.end, processed.transcript);

        int startWordNumber = getWordFromIndex(startPrevDelimiter, processed.indexToWordNumberMap);
        int endWordNumber = getWordFromIndex(endPrevDelimiter, processed.indexToWordNumberMap);

        return getAllWords(startWordNumber, endWordNumber, processed.transcript);
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        int prevIndex = transcript.lastIndexOf(DELIMITER, currentIndex - 1);
        if (prevIndex < 0) {
            return 0;
        } else {
            return prevIndex;
        }
    }

    private int getWordFromIndex(int previousDelimiterIndex, TreeMap<Integer, Integer> indexToWordNumberMap) {
        int key = indexToWordNumberMap.floorKey(previousDelimiterIndex);
        return indexToWordNumberMap.get(key);
    }

    private String getAllWords(int startWordNumber, int endWordNumber, String transcript) {
        List<String> wordList = getWordList(transcript);
        IntegerPair startEndIndexes = highlightMatches(startWordNumber, endWordNumber, wordList);
        return buildStringFromList(wordList, startEndIndexes.start, startEndIndexes.end);
    }

    private List<String> getWordList(String transcript) {
        return new LinkedList<>(Arrays.asList(transcript.split(" ")));
    }

    private IntegerPair highlightMatches(int matchStartIndex, int matchEndIndex, List<String> wordList) {
        String allWordsInMatch = buildStringFromList(wordList, matchStartIndex, matchEndIndex);
        List<IntegerPair> matchIndicesInWord = Searcher.findMatches(allWordsInMatch, printArguments.search);
        IntegerPair newStartEndIndices = removeRangeFromList(matchStartIndex, matchEndIndex, wordList);

        // Codified behavior: when two of the same match exist in the same word, both will be printed on the same
        // match line.
        int matchOffset = 0;
        for (IntegerPair matchPair : matchIndicesInWord) {
            allWordsInMatch = allWordsInMatch.substring(0, matchPair.start + matchOffset) +
                    ANSI_RED_MATCH_HIGHLIGHT +
                    allWordsInMatch.substring(matchPair.start + matchOffset, matchPair.end + matchOffset) +
                    ANSI_RESET +
                    allWordsInMatch.substring(matchPair.end + matchOffset);
            matchOffset += (ANSI_RED_MATCH_HIGHLIGHT.length() + ANSI_RESET.length());
        }

        wordList.add(matchStartIndex, allWordsInMatch);
        return new IntegerPair(getStartWordIndex(newStartEndIndices.start), getEndWordIndex(newStartEndIndices.end, wordList));
    }

    private IntegerPair removeRangeFromList(int start, int end, List<String> wordList) {
        int removeIndex = start;
        for (int i = start; i <= end; ++i) {
            wordList.remove(removeIndex);
        }
        return new IntegerPair(removeIndex, removeIndex);
    }

    private int getStartWordIndex(int matchWordIndex) {
        return ceilToSize(0, matchWordIndex - printArguments.wordsBeforeMatch);
    }

    private int ceilToSize(int size, int value) {
        return value < size ? size : value;
    }

    private int getEndWordIndex(int matchWordIndex, List<String> wordList) {
        return floorToSize(wordList.size() - 1, matchWordIndex + printArguments.wordsAfterMatch);
    }

    private int floorToSize(int size, int value) {
        return size > value ? value : size;
    }

    private String buildStringFromList(List<String> wordList, int start, int end) {
        String result = "";
        for (int i = start; i <= end; ++i) {
            result += wordList.get(i) + " ";
        }
        return result.substring(0, result.length() - 1); // remove trailing space
    }
}
