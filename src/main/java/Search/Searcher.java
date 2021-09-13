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
    private static final int PERIOD_MATCHES_NEWLINES_AND_SPACES = Pattern.DOTALL;
    private static final String MATCH_INDICATOR = "||";
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

    private String getTranscript(Searchable searchable) {
        String transcript = getFileToString(searchable.transcriptFile);
        return stripNewlines(transcript);
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
            int start = stripLeadingDelimiters(matcher.start(), transcript);
            int end = stripTrailingDelimiters(matcher.end(), transcript);
            result.add(new IntegerPair(start, end));
        }
        return result;
    }

    private int stripLeadingDelimiters(int start, String transcript) {
        int newIndex = start;
        while (transcript.charAt(newIndex) == DELIMITER && newIndex < transcript.length() - 1) {
            newIndex++;
        }
        return newIndex;
    }

    private int stripTrailingDelimiters(int end, String transcript) {
        int newIndex = end;
        while (transcript.charAt(newIndex) == DELIMITER && newIndex > 0) {
            newIndex--;
        }
        return newIndex;
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

    private String getSearchedTranscript(String transcript, String search) {
        Matcher matcher = getMatcher(transcript, search);
        int addedMatchesOffset = 0;

        while (matcher.find()) {
            transcript = addMatchIndicators(matcher, transcript, addedMatchesOffset);
            addedMatchesOffset += getMatchIndicatorLength();
        }
        return stripNewlines(transcript);
    }

    Matcher getMatcher(String transcript, String searchString) {
        Pattern regex = getRegex(searchString);
        return regex.matcher(transcript);
    }

    Pattern getRegex(String searchString) {
        String newlineSearchString = replaceSpacesWithNewlines(searchString);
        return Pattern.compile(newlineSearchString, PERIOD_MATCHES_NEWLINES_AND_SPACES);
    }

    private String replaceSpacesWithNewlines(String searchString) {
        return searchString.replace(' ', '\n');
    }

    private String addMatchIndicators(Matcher matcher, String transcript, int indexOffset) {
        int startOfMatch = matcher.start() + indexOffset;
        int endOfMatch = matcher.end() + indexOffset;

        return transcript.substring(0, startOfMatch) +
                MATCH_INDICATOR +
                transcript.substring(startOfMatch, endOfMatch) +
                MATCH_INDICATOR +
                transcript.substring(endOfMatch);
    }

    private String stripNewlines(String input) {
        return input.replace("\n", " ").replace("\r", "");
    }

    private int getMatchIndicatorLength() {
        return MATCH_INDICATOR.length() * 2;
    }
}
