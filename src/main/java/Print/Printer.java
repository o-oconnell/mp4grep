package Print;

import Arguments.PrintArguments;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Printer {
    private static final char DELIMITER = ' ';
    private PrintArguments printArguments;

    public Printer(PrintArguments printArguments) {
        this.printArguments = printArguments;
    }

    public void print(Printable printable) {
        printFilename(printable);
        printMatches(printable);
    }

    private void printFilename(Printable printable) {
        System.out.println("");
        System.out.println("File: " + printable.filename);
        System.out.println("--------");
    }

    private void printMatches(Printable printable) {
        printable.matchIndices.stream()
                .forEach(integerPair -> printMatch(integerPair.start, integerPair.end, printable));
    }

    private void printMatch(int matchStart, int matchEnd, Printable printable) {
        System.out.println(getTimestampPrint(matchStart, printable) + ":" + getTranscriptPrint(matchStart, matchEnd, printable.transcript));
    }

    private String getTimestampPrint(int matchStart, Printable printable) {
        TreeMap<Integer, String> transcriptTimestamps = mapTranscriptToTimestamps(printable.transcript, printable.timestamps);
        int timestampKey = getTimestampKey(transcriptTimestamps, matchStart);
        return transcriptTimestamps.get(timestampKey);
    }

    private String getTranscriptPrint(int matchStart, int matchEnd, String transcript) {
        TreeMap<Integer, Integer> indexToWordNumberMap = new TreeMap<Integer, Integer>();
        List<String> wordList = Arrays.asList(transcript.split(" "));
        List<Integer> delimiterIndices = getDelimiterIndices(transcript);

        var matchWordIndex = new Object() {
            int index = 0;
        };
        delimiterIndices
                .stream()
                .forEach(delimiterIndex -> {
                    indexToWordNumberMap.put(delimiterIndex, matchWordIndex.index);
                    matchWordIndex.index++;
                });

        int previousDelimiterIndex = getPreviousDelimiterIndex(matchEnd, transcript);
        int currentWordNumber = indexToWordNumberMap.get(indexToWordNumberMap.floorKey(previousDelimiterIndex));
        return getAllWords(currentWordNumber, wordList);
    }

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length() - 1)
                .boxed()
                .filter(i -> string.charAt(i) == DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private int getPreviousDelimiterIndex(int currentIndex, String transcript) {
        int prevIndex = transcript.lastIndexOf(DELIMITER, currentIndex - 1);
        if (prevIndex < 0) {
            return 0;
        } else {
            return prevIndex;
        }
    }

    private String getAllWords(int matchWordIndex, List<String> wordList) {
        int start = getStartWordIndex(matchWordIndex, wordList);
        int end = getEndWordIndex(matchWordIndex, wordList);

        String result = "";
        for (int i = start; i <= end; ++i) {
            result += wordList.get(i) + " ";
        }
        return result;
    }

    private int getEndWordIndex(int matchWordIndex, List<String> wordList) {
        int end = matchWordIndex + printArguments.wordsAfterMatch;
        if (end >= wordList.size())
            end = wordList.size() - 1;
        return end;
    }

    private int getStartWordIndex(int matchWordIndex, List<String> wordList) {
        int start = matchWordIndex - printArguments.wordsBeforeMatch;
        if (start < 0)
            start = 0;
        return start;
    }

    private TreeMap<Integer, String> mapTranscriptToTimestamps(String transcript, String timestampsText) {
        TreeMap<Integer, String> transcriptTimestampIndices = new TreeMap<>();
        List<Integer> transcriptDelimiterIndices = getDelimiterIndices(transcript);
        List<String> timestamps = getTimestamps(timestampsText);

        IntStream.range(0, timestamps.size() - 1)
                .boxed()
                .forEach(i -> transcriptTimestampIndices.put(
                        transcriptDelimiterIndices.get(i),
                        timestamps.get(i)
                ));
        return transcriptTimestampIndices;
    }

    private List<String> getTimestamps(String timestampsText) {
        return Arrays.asList(timestampsText.split(" "));
    }

    private int getTimestampKey(TreeMap<Integer, String> transcriptTimestamps, int index) {
        return transcriptTimestamps.floorKey(index);
    }
}
