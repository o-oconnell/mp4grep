package Print;

import java.util.List;
import java.util.TreeMap;

public class Printable {
    public String transcript;
    public String timestamps;
    public String filename;
    List<IntegerPair> matchIndices;
    TreeMap<Integer, IntegerPair> transcriptTimestampIndices;

    public Printable(List<IntegerPair> matchIndices, String transcript, String timestamps, String filename, TreeMap<Integer, IntegerPair> transcriptTimestampIndices) {
        this.matchIndices = matchIndices;
        this.transcript = transcript;
        this.timestamps = timestamps;
        this.filename = filename;
        this.transcriptTimestampIndices = transcriptTimestampIndices;
    }
}
