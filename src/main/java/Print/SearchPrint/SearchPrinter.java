package Print.SearchPrint;

import Arguments.PrintArguments;
import Print.Printable;
import lombok.Builder;
import lombok.NonNull;

import java.util.*;
import java.util.stream.IntStream;

import static Globals.GlobalColors.*;
import static Globals.GlobalErrorCodes.ERROR_EXIT_CODE;

// Responsibility: prints all matches for a file
@Builder
public class SearchPrinter {
    private static final char DELIMITER = ' ';
    private static final String TIMESTAMP_TRANSCRIPT_SEPARATOR = " ";
    @NonNull private PrintArguments printArguments;
    @NonNull private Printable printable;

    public void print() {
        printFilename();
        printMatches();
    }

    private void printFilename() {
        System.out.println();
        System.out.println(ANSI_GREEN + printable.filename + ANSI_RESET);
    }

    private void printMatches() {
        List<String> searchResults = makeSearchResults();
        searchResults.stream().forEach(System.out::println);
    }

    private List<String> makeSearchResults() {
        List<String> timestamps = getFormattedTimestamps(printable);
        List<String> matches = getMatches(printable);
        List<String> result = new ArrayList<>();

        if (timestamps.size() == matches.size()) {
            timestamps.stream().forEach(result::add);
            IntStream.range(0, result.size())
                    .boxed()
                    .forEach(i -> {
                        result.set(i, (result.get(i) + " " + matches.get(i)));
                    });
        } else {
            System.out.println("Error: Timestamp count not equal to match count.");
            System.exit(ERROR_EXIT_CODE);
        }

        return result;
    }

    List<String> getFormattedTimestamps(Printable printable) {
        List<String> timestamps = getTimestamps(printable);
        return TimestampFormatter.format(timestamps);
    }

    List<String> getTimestamps(Printable printable) {
        TimestampProcessor tp = makeTimestampsProcessor(printable);
        return tp.getTimestampMatches();
    }

    private TimestampProcessor makeTimestampsProcessor(Printable printable) {
        return TimestampProcessor
                .builder()
                .printable(printable)
                .printArguments(printArguments)
                .build();
    }

    List<String> getMatches(Printable printable) {
        TranscriptProcessor tp = makeTranscriptProcessor(printable);
        return tp.getTranscriptMatches();
    }

    private TranscriptProcessor makeTranscriptProcessor(Printable printable) {
        return TranscriptProcessor
                .builder()
                .printable(printable)
                .printArguments(printArguments)
                .build();
    }
}
