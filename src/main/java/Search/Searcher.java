package Search;

import java.Search.Searchable;
import Print.Printable;
import Print.IntegerPair;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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
        List<IntegerPair> matches = getMatches(transcript, search);
        TreeMap<Integer, IntegerPair> transcriptTimestampIndices = mapTranscriptToTimestamps(transcript, timestamps);

        return new Printable(matches, transcript, timestamps, searchable.filename, transcriptTimestampIndices);
    }

    private TreeMap<Integer, IntegerPair> mapTranscriptToTimestamps(String transcript, String timestamps) {
        TreeMap<Integer, IntegerPair> transcriptTimestampIndices = new TreeMap<>();

        int timestampStart = 0;
        int timestampEnd = getNextDelimiterIndex(timestampStart, timestamps);

        // base case for the first transcript word, which does not have a prior space
        IntegerPair firstTimestamp = new IntegerPair(timestampStart, timestampEnd);
        transcriptTimestampIndices.put(0, firstTimestamp);

        for (int transcriptIndex = 0; transcriptIndex < transcript.length(); ++transcriptIndex) {
            if (transcript.charAt(transcriptIndex) == DELIMITER) {
                timestampStart = timestampEnd + 1;
                timestampEnd = getNextDelimiterIndex(timestampStart, timestamps);
                IntegerPair timestamp = new IntegerPair(timestampStart, timestampEnd);

                if (timestampStart < timestampEnd) {
                    transcriptTimestampIndices.put(transcriptIndex, timestamp);
                }
            }
        }
        return transcriptTimestampIndices;
    }

    int getNextDelimiterIndex(int current, String string) {
        int nextDelimiter = string.indexOf(DELIMITER, current + 1);
        if (nextDelimiter > current && nextDelimiter < string.length()) {
            return nextDelimiter;
        } else {
            return string.length() - 1;
        }
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
            int start = matcher.start();
            int end = matcher.end();
            result.add(new IntegerPair(start, end));
        }
        return result;
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
