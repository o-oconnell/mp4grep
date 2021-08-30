package Search;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Output.MultithreadedPrinter;
import org.apache.commons.io.FileUtils;

public class GreppableTranscript implements Greppable {

    private static final int PERIOD_MATCHES_NEWLINES_AND_SPACES = Pattern.DOTALL;

    private String timestampFilename;
    private File contentFile;
    private int nWordsPrior;
    private int nWordsAfter;
    private String transcript;
    private String highlightedTranscript;
    private String inputFilename;

    public GreppableTranscript(CacheKey cacheKey,
                               int nWordsPrior,
                               int nWordsAfter) {
        this.timestampFilename = cacheKey.getTimestampFilename();
        this.contentFile = cacheKey.getTranscriptFile();
        this.nWordsAfter = nWordsAfter;
        this.nWordsPrior = nWordsPrior;
        this.transcript = getTranscript();
        this.inputFilename = cacheKey.filename;
    }

    public void search(String searchString) {

        Matcher matcher = getMatcher(searchString);

        ArrayList<String> matches = new ArrayList<String>();
        setFileSearchHeading(matches);

        while (matcher.find()) {
            this.highlightedTranscript = getHighlightedTranscript(matcher);
            String match = getMatchString(matcher);
            matches.add(match);
        }
        MultithreadedPrinter.add(matches);
    }

    private void setFileSearchHeading(ArrayList<String> matches) {
        matches.add("");
        matches.add("File: " + inputFilename);
        matches.add("-------------");
    }

    private String replaceSpacesWithNewlines(String searchString) {
        return searchString.replace(' ', '\n');
    }

    Pattern getRegex(String searchString) {
        String newlineSearchString = replaceSpacesWithNewlines(searchString);
        return Pattern.compile(newlineSearchString, PERIOD_MATCHES_NEWLINES_AND_SPACES);
    }

    Matcher getMatcher(String searchString) {
        Pattern regex = getRegex(searchString);
        String transcript = getTranscript();
        return regex.matcher(transcript);
    }

    String getTranscript() {
        String transcript = null;
        String charset = null; // indicates using platform default

        try {
            transcript = FileUtils.readFileToString(contentFile, charset);
        } catch (IOException e) {
            System.out.println("Error reading transcription file to string.");
            e.printStackTrace();
        }

        return transcript;
    }

    private String getMatchString(Matcher matcher) {
        String searchResult = getSearchResult(matcher);
        String timestamp = getTimestamp(matcher);

        return timestamp + " " + searchResult;
    }

    String getSearchResult(Matcher matcher) {
        int searchResultStart = getSearchStart(matcher);
        int searchResultEnd = getSearchEnd(matcher);

        String resultWithNewlines = highlightedTranscript.substring(searchResultStart, searchResultEnd + 1);
        return stripNewlines(resultWithNewlines);
    }

    String getTimestamp(Matcher matcher) {

        int firstWordColumn = getColumnNumberOfFirstWord(matcher);
        BufferedReader timestampReader = null;

        try {
            timestampReader = new BufferedReader(new FileReader(timestampFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Stream<String> timestampStream = timestampReader.lines();
        List<String> timestampList = timestampStream.collect(Collectors.toList());

        return timestampList.get(firstWordColumn);
    }

    int getColumnNumberOfFirstWord(Matcher matcher) {
        int startOfMatch = getMatchStartIndexForHighlightedMatchString(matcher);
        int newlineBeforeFirstWordIndex = getSearchStart(matcher);

        if (newlineBeforeFirstWordIndex == 0) {
            return 0;
        } else {
            return getColumnNumberFromNewlineIndex(newlineBeforeFirstWordIndex);
        }
    }

    private int getColumnNumberFromNewlineIndex(int newlineIndex) {
        long numberColumns = transcript
                .substring(0, newlineIndex + 1)
                .chars()
                .filter(ch -> ch == '\n')
                .count();

        return (int) numberColumns; // (zero-indexed column number of word)
    }

    private int getSearchEnd(Matcher matcher) {
        int matchEndIndex = getMatchEndIndexForHighlightedMatchString(matcher);

        int index = matchEndIndex;

        for (int i = 0; i < nWordsAfter + 1; ++i) {
            if (nextNewLineIndexOutOfStringBounds(highlightedTranscript, index)) {
                index = highlightedTranscript.length();
                break;
            } else {
                index = highlightedTranscript.indexOf('\n', index);
                index = index + 1;
            }
        }

        index--; // oversteps by one char on the last iteration

        return indexOrStringEnd(index, highlightedTranscript);
    }

    boolean nextNewLineIndexOutOfStringBounds(String contentString, int index) {
       if (index >= contentString.length() || contentString.indexOf('\n', index) < index)
           return true;
       else
           return false;
    }

    private int indexOrStringEnd(int index, String contentString) {
        if (index == -1) {
            return contentString.length() - 1;
        } else {
            return index;
        }
    }

    private int getSearchStart(Matcher matcher) {

        int matchStartIndex = getMatchStartIndexForHighlightedMatchString(matcher);

        int index = matchStartIndex;
        for (int i = 0; i < nWordsPrior + 1; ++i) {
            index = highlightedTranscript.lastIndexOf('\n', index);
            index = index - 1;
        }

        index++; // understeps by one char on the last iteration

        return indexOrStringBeginning(index);
    }

    private int indexOrStringBeginning(int index) {
        if (index < 0) {
            return 0;
        } else {
            return index;
        }
    }

    private String getHighlightedTranscript(Matcher matcher) {
        int startOfMatch = matcher.start();
        int endOfMatch = matcher.end();

        return transcript.substring(0, startOfMatch) +
                "||" +
                transcript.substring(startOfMatch, endOfMatch) +
                "||" +
                transcript.substring(endOfMatch);
    }

    private String stripNewlines(String input) {
        return input.replace("\n", " ").replace("\r", "");
    }

    private int getMatchEndIndexForHighlightedMatchString(Matcher matcher) {
        int newLength = highlightedTranscript.length() - transcript.length();
        return matcher.end() + newLength;
    }

    private int getMatchStartIndexForHighlightedMatchString(Matcher matcher) {
        return matcher.start();
    }
}
