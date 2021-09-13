package Print;

import lombok.Builder;

import java.util.List;
import java.util.TreeMap;

@Builder
public class Printable {
    public String transcript;
    public String filename;
    List<IntegerPair> matchIndices;
    TreeMap<Integer, String> transcriptTimestamps;
}
