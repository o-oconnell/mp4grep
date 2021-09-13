package Search;

import Print.Printable;
import Print.IntegerPair;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class Searcher {
    private static final String DEFAULT_CHARSET = null; // Indicates platform default for FileUtils
    private static final char DELIMITER = ' ';

    public Printable getPrintableSearchResult(Searchable searchable, String search) {
        String transcript = getTranscript(searchable);
        String timestamps = getTimestamps(searchable.timestampFile);

        return Printable
                .builder()
                .transcript(transcript)
                .filename(searchable.filename)
                .matchIndices(getMatches(transcript, search))
                .transcriptTimestamps(mapTranscriptToTimestamps(transcript, timestamps))
                .build();
    }

    private String getTranscript(Searchable searchable) {
        String transcript = getFileToString(searchable.transcriptFile);
        return stripNewlines(transcript);
    }

    private String getFileToString(File file) {
        String result = null;
        try {
            result = FileUtils.readFileToString(file, DEFAULT_CHARSET);
        } catch (IOException e) {
            System.out.println("Error reading transcription file to string.");
            e.printStackTrace();
        }
        return result;
    }

    private String getTimestamps(File timestampFile) {
        String timestamps = getFileToString(timestampFile);
        return stripNewlines(timestamps);
    }

    private List<IntegerPair> getMatches(String transcript, String search) {
        List<IntegerPair> result = new LinkedList<IntegerPair>();
        Pattern pattern = Pattern.compile(search);
        Matcher matcher = pattern.matcher(transcript);

        while (matcher.find()) {
            result.add(new IntegerPair(matcher.start(), matcher.end()));
        }
        return result;
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

    private List<Integer> getDelimiterIndices(String string) {
        List<Integer> indices = IntStream.range(0, string.length()-1)
                .boxed()
                .filter(i -> string.charAt(i) == ' ')
                .collect(Collectors.toList());
        indices.add(0, 0);
        return indices;
    }

    private List<String> getTimestamps(String timestampsText) {
        return Arrays.asList(timestampsText.split(" "));
    }

    private String stripNewlines(String input) {
        return input.replace("\n", " ").replace("\r", "");
    }
}
