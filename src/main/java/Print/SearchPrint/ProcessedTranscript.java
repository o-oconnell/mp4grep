package Print.SearchPrint;

import java.util.List;
import java.util.TreeMap;

public class ProcessedTranscript {
    public String transcript;
    public TreeMap<Integer, Integer> indexToWordNumberMap;

    public ProcessedTranscript(String transcript, TreeMap<Integer, Integer> indexToWordNumberMap) {
        this.transcript = transcript;
        this.indexToWordNumberMap = indexToWordNumberMap;
    }
}
