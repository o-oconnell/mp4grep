package Print;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.IntStream;

// The sole purpose of this class is to compute the "processed form" (a form that allows
// obtaining a timestamp corresponding to a transcript word with minimal computation) of the timestamps
// which is the transcriptTimestampMap treemap that maps transcript indices to timestamp strings
public class ProcessedTimestamps {
    private static char DELIMITER = ' ';

    public TreeMap<Integer, String> transcriptTimestampMap;

    public ProcessedTimestamps(String timestampsText, List<Integer> delimiterIndices) {
        this.transcriptTimestampMap = mapTranscriptToTimestamps(timestampsText, delimiterIndices);
    }

    private TreeMap<Integer, String> mapTranscriptToTimestamps(String timestampsText, List<Integer> transcriptDelimiterIndices) {
        TreeMap<Integer, String> transcriptTimestampIndices = new TreeMap<>();
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
}
