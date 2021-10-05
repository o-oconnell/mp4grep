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
    private static final String FILE_UTILS_PLATFORM_DEFAULT_CHARSET = null;

    public Printable getPrintableSearchResult(Searchable searchable, String search) {
        String transcript = getContentsWithoutNewlines(searchable.transcriptFile);
        String timestamps = getContentsWithoutNewlines(searchable.timestampFile);

        return Printable
                .builder()
                .transcript(transcript)
                .timestamps(timestamps)
                .filename(searchable.filename)
                .matchIndices(findMatches(transcript, search))
                .build();
    }

    private static String getContentsWithoutNewlines(File file) {
        String result = null;
        try {
            result = FileUtils.readFileToString(file, FILE_UTILS_PLATFORM_DEFAULT_CHARSET);
        } catch (IOException e) {
            System.out.println("Error reading transcription file to string.");
            e.printStackTrace();
        }
        return stripNewlines(result);
    }

    private static String stripNewlines(String input) {
        return input.replace("\n", " ").replace("\r", "");
    }

    private static List<IntegerPair> findMatches(String transcript, String search) {
        List<IntegerPair> result = new LinkedList<>();
        Pattern pattern = Pattern.compile(search);
        Matcher matcher = pattern.matcher(transcript);

        while (matcher.find()) {
            result.add(new IntegerPair(matcher.start(), matcher.end()));
        }
        return result;
    }
}
