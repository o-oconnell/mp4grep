package Search;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

public class GreppableTranscript implements Greppable {

    private String timestampFilename;
    private File contentFile;
    private int nWordsPrior;
    private int nWordsAfter;

    public GreppableTranscript(String timestampFilename,
                               String contentFilename,
                               int nWordsPrior,
                               int nWordsAfter) {

        this.timestampFilename = timestampFilename;
        this.contentFile = new File(contentFilename);
        this.nWordsAfter = nWordsAfter;
        this.nWordsPrior = nWordsPrior;
    }

    public void search(String searchString) {

        String newlineSearchString = replaceSpacesWithNewlines(searchString);

        Pattern regex = getRegex(newlineSearchString);
        String transcript = getTranscript();
        Matcher matcher = regex.matcher(transcript);

        while (matcher.find()) {
            String match = getMatchString(matcher, transcript);
            System.out.println(match);
        }
    }

    private String replaceSpacesWithNewlines(String searchString) {
        return searchString.replace(' ', '\n');
    }

    Pattern getRegex(String newlineSearchString) {
        // DOTALL allows the "." in a regular expression to match newlines and spaces
        return Pattern.compile(newlineSearchString, Pattern.DOTALL);
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

    private String getMatchString(Matcher matcher, String contentString) {
        int startOfMatch = matcher.start();
        int endOfMatch = matcher.end();

        String highlightedMatchString = getHighlightedMatchString(contentString, matcher);
        int searchResultStartIndex = getSearchResultStartIndex(startOfMatch, highlightedMatchString);
        int searchResultEndIndex = getSearchResultEndIndex(endOfMatch, highlightedMatchString);

        String resultWithNewlines =
                highlightedMatchString.substring(searchResultStartIndex, searchResultEndIndex);
        String resultWithoutNewLines = stripNewlines(resultWithNewlines);

        int firstWordColumn = getColumnNumberOfFirstWord(contentString, matcher);
        String timestamp = getTimestamp(firstWordColumn, resultWithoutNewLines);

        return timestamp + " " + resultWithoutNewLines;
    }

    String getTimestamp(int firstWordColumn, String content) {

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

    int getColumnNumberOfFirstWord(String contentString, Matcher matcher) {
        int startOfMatch = matcher.start();
        int newlineBeforeFirstWordIndex = getSearchResultStartIndex(startOfMatch, contentString);

        if (newlineBeforeFirstWordIndex == 0) {
            return 0;
        } else {
            return getColumnNumberFromNewlineIndex(newlineBeforeFirstWordIndex, contentString);
        }
    }

    private int getColumnNumberFromNewlineIndex(int newlineIndex, String contentString) {
        long numberColumns = contentString
                .substring(0, newlineIndex + 1)
                .chars()
                .filter(ch -> ch == '\n')
                .count();

        return (int) numberColumns; // (zero-indexed column number of word)
    }

    private int getSearchResultEndIndex(int currentIndex, String contentString) {

        int index = currentIndex;

        for (int i = 0; i < nWordsAfter + 1; ++i) {
            if (index >= contentString.length()) {
                // this if statement is necessary because indexOf at the end of a string will start again at the string's beginning
                index = contentString.length();
                break;
            } else {
                index = contentString.indexOf('\n', index);
                index = index + 1;
            }
        }

        index--; // oversteps by one char on the last iteration
        return indexOrStringEnd(index, contentString);
    }

    private int indexOrStringEnd(int index, String contentString) {
        if (index == -1) {
            return contentString.length() - 1;
        } else {
            return index;
        }
    }

    private int getSearchResultStartIndex(int currentIndex, String contentString) {

        int index = currentIndex;
        for (int i = 0; i < nWordsPrior + 1; ++i) {
            index = contentString.lastIndexOf('\n', index);
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

    private String getHighlightedMatchString(String contentString, Matcher matcher) {
        int startOfMatch = matcher.start();
        int endOfMatch = matcher.end();

        return contentString.substring(0, startOfMatch) +
                "||" +
                contentString.substring(startOfMatch, endOfMatch) +
                "||" +
                contentString.substring(endOfMatch);
    }

    private String stripNewlines(String input) {
        return input.replace("\n", " ").replace("\r", "");
    }

    @Override
    public void storeTranscriptInLocation(File transcriptFile) {
        if (transcriptFile.exists())
            transcriptFile.delete();
        else {
            File transcriptResult = contentFile;

            Path destination = Paths.get(transcriptFile.toString());
            Path source = Paths.get(transcriptResult.toString());

            try {
                Path result = Files.move(source, destination);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void storeTimestampsInLocation(File timestampFile) {
        if (timestampFile.exists())
            timestampFile.delete();
        else {
            File timestampResult = new File(timestampFilename);

            Path destination = Paths.get(timestampFile.toString());
            Path source = Paths.get(timestampResult.toString());

            try {
                Path result = Files.move(source, destination);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
