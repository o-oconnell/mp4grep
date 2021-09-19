package Print;

import java.util.List;
import java.util.TreeMap;

public class ProcessedTranscript {
    private static final char DELIMITER = ' ';

    public String transcript;
    public TreeMap<Integer, Integer> indexToWordNumberMap;

    public ProcessedTranscript(String transcript, List<Integer> delimiterIndices) {
        this.transcript = transcript;
        this.indexToWordNumberMap = getWordNumberMap(delimiterIndices);
    }

    private TreeMap<Integer, Integer> getWordNumberMap(List<Integer> delimiterIndices) {
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
}
