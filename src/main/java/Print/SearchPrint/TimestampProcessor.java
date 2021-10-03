package Print.SearchPrint;

import Arguments.PrintArguments;
import Print.IntegerPair;
import Print.Printable;
import lombok.Builder;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Finds all of the correct timestamps to print out for a printable
@Builder
public class TimestampProcessor {
    private final char TRANSCRIPT_DELIMITER = ' ';
    private TreeMap<Integer, String> transcriptTimestampMap;

    @NonNull private Printable printable;
    @NonNull private PrintArguments printArguments;

    public List<String> getTimestampMatches() {
        this.transcriptTimestampMap = getTranscriptTimestampMap();
        return printable.matchIndices.stream()
                .map(this::getTimestampPrint)
                .collect(Collectors.toList());
    }

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

    private List<Integer> getTranscriptDelimiterIndices(String transcript) {
        List<Integer> indices = IntStream.range(0, transcript.length() - 1)
                .boxed()
                .filter(i -> transcript.charAt(i) == TRANSCRIPT_DELIMITER)
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private List<String> getTimestamps(String timestampsText) {
        return Arrays.asList(timestampsText.split(" "));
    }

    public String getTimestampPrint(IntegerPair matchPair) {
        int index = transcriptTimestampMap.floorKey(matchPair.start);
        String timestamp = transcriptTimestampMap.get(index);

        for (int i = 0; i < printArguments.wordsBeforeMatch; ++i) {
            Integer prevKey = transcriptTimestampMap.lowerKey(index);
            if (prevKey != null) {
                index = prevKey;
                timestamp = transcriptTimestampMap.get(prevKey);
            }
        }
        return timestamp;
    }
}
