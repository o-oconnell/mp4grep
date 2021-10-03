package Print.SearchPrint;

import Arguments.PrintArguments;
import Print.IntegerPair;
import Print.Printable;
import Search.Searcher;
import lombok.Builder;
import lombok.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Globals.GlobalColors.ANSI_RED;
import static Globals.GlobalColors.ANSI_RESET;

@Builder
public class TranscriptProcessor {
    private final char TRANSCRIPT_DELIMITER = ' ';
    private TreeMap<Integer, Integer> indexToWordNumberMap;

    @NonNull private Printable printable;
    @NonNull private PrintArguments printArguments;

    public List<String> getTranscriptMatches() {
        this.indexToWordNumberMap = makeIndexToWordNumberMap();
        return printable.matchIndices.stream()
                .map(this::getTranscriptPrint)
                .collect(Collectors.toList());
    }

    private TreeMap<Integer, Integer> makeIndexToWordNumberMap() {
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
        return wordNumberMap;
    }

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length() - 1)
                .boxed()
                .filter(i -> string.charAt(i) == TRANSCRIPT_DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    public String getTranscriptPrint(IntegerPair matchPair) {
        int startPrevDelimiter = getPreviousDelimiterIndex(matchPair.start, printable.transcript);
        int endPrevDelimiter = getPreviousDelimiterIndex(matchPair.end, printable.transcript);

        int startWordNumber = getWordFromIndex(startPrevDelimiter, indexToWordNumberMap);
        int endWordNumber = getWordFromIndex(endPrevDelimiter, indexToWordNumberMap);

        return getAllWords(startWordNumber, endWordNumber, printable.transcript);
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        int prevIndex = transcript.lastIndexOf(TRANSCRIPT_DELIMITER, currentIndex - 1);
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
