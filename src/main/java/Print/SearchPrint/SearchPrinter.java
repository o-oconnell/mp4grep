package Print.SearchPrint;

import Arguments.PrintArguments;
import Print.IntegerPair;
import Print.Printable;
import lombok.Builder;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Globals.GlobalColors.*;

// Responsibility: prints all matches for a file
@Builder
public class SearchPrinter {
    private final char TRANSCRIPT_DELIMITER = ' ';
    @NonNull private PrintArguments printArguments;
    @NonNull private Printable printable;

    public void print() {
        printFilename();
        printMatches();
    }

    private void printFilename() {
        System.out.println();
        System.out.println(ANSI_GREEN + printable.filename + ANSI_RESET);
    }

    private void printMatches() {
        TreeMap<Integer, Integer> indexToWordNumberMap = makeIndexToWordNumberMap();
        TreeMap<Integer, String> transcriptTimestampMap = makeTranscriptTimestampMap();
        List<String> transcriptWordList = getWordList(printable.transcript);

        for (IntegerPair matchPair : printable.matchIndices) {
            String timestamp = getTimestamp(matchPair, transcriptTimestampMap);
            String wordsBefore = getWordsBefore(matchPair, indexToWordNumberMap, transcriptWordList); // words before the first word in which the match is contained
            String match = getMatch(matchPair); // all words across which the match spans, with the match highlighted
            String wordsAfter = getWordsAfter(matchPair, indexToWordNumberMap, transcriptWordList); // words after the last word in which the match is contained

            printResult(timestamp, wordsBefore, match, wordsAfter);
        }
    }

    private void printResult(String timestamp, String wordsBefore, String match, String wordsAfter) {
        String result = timestamp + " ";
        if (wordsBefore.length() > 0) {
            result += wordsBefore + " ";
        }
        result += match;
        if (wordsAfter.length() > 0) {
            result += " " + wordsAfter;
        }
        System.out.println(result);
    }

    private TreeMap<Integer, Integer> makeIndexToWordNumberMap() {
        List<Integer> delimiterIndices = getTranscriptDelimiterIndices(printable.transcript);
        TreeMap<Integer, Integer> wordNumberMap = new TreeMap<>();

        IntStream.range(0, delimiterIndices.size()).boxed()
                .forEach(i -> {
                    wordNumberMap.put(delimiterIndices.get(i), i);
                });

        return wordNumberMap;
    }

    private List<Integer> getTranscriptDelimiterIndices(String transcript) {
        List<Integer> indices = IntStream.range(0, transcript.length()).boxed()
                .filter(i -> transcript.charAt(i) == TRANSCRIPT_DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private TreeMap<Integer, String> makeTranscriptTimestampMap() {
        List<Integer> transcriptDelimiterIndices = getTranscriptDelimiterIndices(printable.transcript);
        TreeMap<Integer, String> transcriptTimestampMap = new TreeMap<>();
        List<String> timestamps = getTimestamps(printable.timestamps);

        IntStream.range(0, timestamps.size())
                .boxed()
                .forEach(i -> transcriptTimestampMap.put(
                        transcriptDelimiterIndices.get(i),
                        timestamps.get(i)
                ));
        return transcriptTimestampMap;
    }

    private List<String> getTimestamps(String timestampsText) {
        return Arrays.asList(timestampsText.split(" "));
    }

    private List<String> getWordList(String transcript) {
        return new LinkedList<>(Arrays.asList(transcript.split(" ")));
    }

    public String getTimestamp(IntegerPair matchPair, TreeMap<Integer, String> transcriptTimestampMap) {
        int index = transcriptTimestampMap.floorKey(matchPair.start);

        for (int i = 0; i < printArguments.wordsBeforeMatch; ++i) {
            Integer prevKey = transcriptTimestampMap.lowerKey(index);

            if (prevKey == null) {
                break;
            }
            index = prevKey;
        }

        return ANSI_BLUE + "[" + transcriptTimestampMap.get(index) + "]" + ANSI_RESET;
    }

    private String getWordsBefore(IntegerPair matchPair, TreeMap<Integer, Integer> indexToWordNumberMap, List<String> wordList) {
        int matchWordIndex = getWordNumberForIndex(matchPair.start, indexToWordNumberMap);
        int start = matchWordIndex - printArguments.wordsBeforeMatch;
        int end = matchWordIndex - 1;

        if (start < 0 && end >= 0) {
            return buildStringFromList(new IntegerPair(0, end), wordList);
        } else if (start < 0 || start > end) {
            return "";
        } else {
            return buildStringFromList(new IntegerPair(start, end), wordList);
        }
    }

    private int getWordNumberForIndex(int index, TreeMap<Integer, Integer> indexToWordNumberMap) {
        int prevDelimiterIndex = getPreviousDelimiterIndex(index, printable.transcript);
        int key = indexToWordNumberMap.floorKey(prevDelimiterIndex);
        return indexToWordNumberMap.get(key);
    }

    String buildStringFromList(IntegerPair indexRangeInclusive, List<String> wordList) {
        String output = "";
        for (int i = indexRangeInclusive.start; i <= indexRangeInclusive.end; ++i) {
            output += wordList.get(i) + " ";
        }
        return output.substring(0, output.length() - 1); // Trim the final space character from the string.
    }

    private String getMatch(IntegerPair matchPair) {
        // Get the indexes in the transcript string across which the match extends
        int matchWordsStart = getWordStartFromIndex(matchPair.start, printable.transcript);
        int matchWordsEnd = getWordEndFromIndex(matchPair.end, printable.transcript);

        // Get the substring across which the match extends
        String allMatchWords = printable.transcript.substring(matchWordsStart, matchWordsEnd); // +1 because both indexes are inclusive.

        // Highlight the match in the substring
        return highlightMatch(matchPair, allMatchWords, matchWordsStart);
    }

    private int getWordStartFromIndex(int index, String transcript) {
        int prevDelimiter = getPreviousDelimiterIndex(index, transcript);
        if (transcript.charAt(prevDelimiter) == TRANSCRIPT_DELIMITER) {
            return prevDelimiter + 1;
        } else {
            return prevDelimiter; // In this case, we have reached the start of the string, which is the start index of the word.
        }
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        int prevIndex = transcript.lastIndexOf(TRANSCRIPT_DELIMITER, currentIndex - 1);
        if (prevIndex == -1) {
            return 0;
        } else {
            return prevIndex;
        }
    }

    private int getWordEndFromIndex(int index, String transcript) {
        int nextDelimiter = getNextDelimiterIndex(index, transcript);
        return nextDelimiter;
    }

    private int getNextDelimiterIndex(int currentIndex, String transcript) {
        int nextIndex = transcript.indexOf(TRANSCRIPT_DELIMITER, currentIndex);
        if (nextIndex == -1) {
            return transcript.length();
        } else {
            return nextIndex;
        }
    }

    private String highlightMatch(IntegerPair matchPair, String allMatchWords, int matchWordsStart) {
        int start = matchPair.start - matchWordsStart;
        int end = matchPair.end - matchWordsStart;

        return allMatchWords.substring(0, start)
                + ANSI_RED
                + allMatchWords.substring(start, end)
                + ANSI_RESET
                + allMatchWords.substring(end);
    }

    private String getWordsAfter(IntegerPair matchPair, TreeMap<Integer, Integer> indexToWordNumberMap, List<String> wordList) {
        int matchWordIndex = getWordNumberForIndex(matchPair.end, indexToWordNumberMap);
        int start = matchWordIndex + 1;
        int end = matchWordIndex + printArguments.wordsAfterMatch;

        if (start < wordList.size() && end >= wordList.size()) {
            return buildStringFromList(new IntegerPair(start, wordList.size() - 1), wordList);
        } else if (start >= wordList.size() || start > end) {
            return "";
        } else {
            return buildStringFromList(new IntegerPair(start, end), wordList);
        }
    }
}
