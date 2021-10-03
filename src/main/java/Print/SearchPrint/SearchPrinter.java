package Print.SearchPrint;

import Arguments.PrintArguments;
import Globals.GlobalErrorCodes;
import Print.IntegerPair;
import Print.Printable;
import Search.Searcher;
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
        List<String> searchResults = makeSearchResults();
        searchResults.stream().forEach(System.out::println);
    }

    private List<String> makeSearchResults() {
        List<String> timestamps = getFormattedTimestamps(printable);
        List<String> matches = getFormattedMatches(printable);
        List<String> result = new ArrayList<>();

        if (timestamps.size() == matches.size()) {
            timestamps.stream().forEach(result::add);
            IntStream.range(0, result.size())
                    .boxed()
                    .forEach(i -> {
                        result.set(i, (result.get(i) + " " + matches.get(i)));
                    });
        } else {
            System.out.println("Error: Timestamp count not equal to match count.");
            System.exit(ERROR_EXIT_CODE);
        }

        return result;
    }

    List<String> getFormattedTimestamps(Printable printable) {
        List<String> timestamps = getTimestamps(printable);
        return TimestampFormatter.format(timestamps);
    }

    List<String> getTimestamps(Printable printable) {
        TimestampsProcessor tp = makeTimestampsProcessor(printable);
        return tp.getTimestampMatches();
    }

    private TimestampsProcessor makeTimestampsProcessor(Printable printable) {
        return TimestampsProcessor
                .builder()
                .printable(printable)
                .printArguments(printArguments)
                .build();
    }

    List<String> getFormattedMatches(Printable printable) {
        List<String> matches = getMatches(printable);
        return TranscriptFormatter.format(matches);
    }

    List<String> getMatches(Printable printable) {
        TranscriptProcessor tp = makeTranscriptProcessor(printable);
        return tp.getTranscriptMatches();
    }

    private TranscriptProcessor makeTranscriptProcessor(Printable printable) {
        return TranscriptProcessor
                .builder()
                .printable(printable)
                .printArguments(printArguments)
                .build();
    }

    private ProcessedTranscript makeProcessedTranscript(Printable printable) {
        List<Integer> delimiterIndices = getDelimiterIndices(printable.transcript);
        TreeMap<Integer, Integer> wordNumberMap = new TreeMap<>();

        var matchWordIndex = new Object() {
            int index = 0;
        };
        delimiterIndices
                .stream()
                .forEach(delimiterIndex -> {
                    wordNumberMap.put(delimiterIndex, matchWordIndex.index);
                    matchWordIndex.index++;
                });
        return new ProcessedTranscript(printable.transcript, wordNumberMap);
    }

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length() - 1)
                .boxed()
                .filter(i -> string.charAt(i) == DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

//    private void printMatch(IntegerPair matchPair, ProcessedTranscript processedTranscript, ProcessedTimestamps processedTimestamps) {
//        System.out.println(
//                ANSI_BLUE + "[" + getTimestampPrint(matchPair, processedTimestamps) + "] " + ANSI_RESET
//                + getTranscriptPrint(matchPair, processedTranscript));
//    }

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
                    ANSI_RED +
                    allWordsInMatch.substring(matchPair.start + matchOffset, matchPair.end + matchOffset) +
                    ANSI_RESET +
                    allWordsInMatch.substring(matchPair.end + matchOffset);
            matchOffset += (ANSI_RED.length() + ANSI_RESET.length());
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
