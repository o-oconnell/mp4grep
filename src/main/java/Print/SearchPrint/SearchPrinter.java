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
import static Globals.GlobalErrorCodes.ERROR_EXIT_CODE;

// Responsibility: prints all matches for a file
@Builder
public class SearchPrinter {
    private static final char DELIMITER = ' ';
    private static final String TIMESTAMP_TRANSCRIPT_SEPARATOR = " ";
    private final char TRANSCRIPT_DELIMITER = ' ';
    @NonNull private PrintArguments printArguments;
    @NonNull private Printable printable;
    TreeMap<Integer, Integer> indexToWordNumberMap;
    TreeMap<Integer, String> transcriptTimestampMap;

    private TreeMap<Integer, String> getTranscriptTimestampMap() {
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

    private List<Integer> getTranscriptDelimiterIndices(String transcript) {
        List<Integer> indices = IntStream.range(0, transcript.length()).boxed()
                .filter(i -> transcript.charAt(i) == TRANSCRIPT_DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private TreeMap<Integer, Integer> makeIndexToWordNumberMap() {
        List<Integer> delimiterIndices = getDelimiterIndices(printable.transcript);
        TreeMap<Integer, Integer> wordNumberMap = new TreeMap<>();

        IntStream.range(0, delimiterIndices.size()).boxed()
                .forEach(i -> {
                    wordNumberMap.put(delimiterIndices.get(i), i);
                });

        return wordNumberMap;
    }

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length())
                .boxed()
                .filter(i -> string.charAt(i) == TRANSCRIPT_DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    public void print() {
        printFilename();
        printMatches();
    }

    private void printFilename() {
        System.out.println();
        System.out.println(ANSI_GREEN + printable.filename + ANSI_RESET);
    }

    private void printMatches() {
//        List<String> searchResults = makeSearchResults();
//        searchResults.stream().forEach(System.out::println);
        // TODO:
// For loop, that does each of the four necessary things individually
// (preprocessing occurs before for loop to create maps)
// -get the words before
// -get the words after
// -highlight the match
// -combine the timestamp into the string
        this.indexToWordNumberMap = makeIndexToWordNumberMap();
        this.transcriptTimestampMap = getTranscriptTimestampMap();

        for (IntegerPair matchPair : printable.matchIndices) {
            String timestamp = getTimestamp(matchPair);
            String wordsBefore = getWordsBefore(matchPair); // words before the first word in which the match is contained
            String match = getMatch(matchPair); // all words across which the match spans, with the match highlighted
            String wordsAfter = getWordsAfter(matchPair); // words after the last word in which the match is contained
            
            System.out.println(timestamp + " " + wordsBefore + " " + match + " " + wordsAfter);
        }
    }

    public String getTimestamp(IntegerPair matchPair) {
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

    private String getMatch(IntegerPair matchPair) {
        // Get the indexes in the transcript string across which the match extends
        int matchWordsStart = getWordStartFromIndex(matchPair.start, printable.transcript);
        int matchWordsEnd = getWordEndFromIndex(matchPair.end, printable.transcript);

        // Get the substring across which the match extends
        String allMatchWords = printable.transcript.substring(matchWordsStart, matchWordsEnd); // +1 because both indexes are inclusive.

        // Highlight the match in the substring
        return highlightMatch(matchPair, allMatchWords, matchWordsStart);
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

    private String getWordsBefore(IntegerPair matchPair) {
        int matchWordIndex = getWordNumberForIndex(matchPair.start, indexToWordNumberMap);
        int start = Math.max(0, matchWordIndex - printArguments.wordsBeforeMatch);
        int end = Math.max(0, matchWordIndex - 1);
        return buildStringFromList(new IntegerPair(start, end), getWordList(printable.transcript));
    }

    private String getWordsAfter(IntegerPair matchPair) {
        int matchWordIndex = getWordNumberForIndex(matchPair.end, indexToWordNumberMap);
        int start = Math.min(printable.transcript.length() - 1, matchWordIndex + 1);
        int end = Math.min(printable.transcript.length() - 1, matchWordIndex + printArguments.wordsAfterMatch);
        return buildStringFromList(new IntegerPair(start, end), getWordList(printable.transcript));
    }

    String buildStringFromList(IntegerPair indexRangeInclusive, List<String> wordList) {
        String output = "";
        for (int i = indexRangeInclusive.start; i <= indexRangeInclusive.end; ++i) {
            output += wordList.get(i) + " ";
        }
        return output.length() == 0 ? output : output.substring(0, output.length() - 1);
    }

    private int getWordNumberForIndex(int index, TreeMap<Integer, Integer> indexToWordNumberMap) {
        int prevDelimiterIndex = getPreviousDelimiterIndex(index, printable.transcript);
        int key = indexToWordNumberMap.floorKey(prevDelimiterIndex);
        return indexToWordNumberMap.get(key);
    }

    private List<String> getWordList(String transcript) {
        return new LinkedList<>(Arrays.asList(transcript.split(" ")));
    }
}
