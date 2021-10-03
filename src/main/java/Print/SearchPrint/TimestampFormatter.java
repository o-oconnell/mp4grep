package Print.SearchPrint;

import java.util.List;
import java.util.stream.IntStream;

import static Globals.GlobalColors.ANSI_BLUE;
import static Globals.GlobalColors.ANSI_RESET;

public class TimestampFormatter {
    public static List<String> format(List<String> timestamps) {
        IntStream.range(0, timestamps.size())
                .forEach(i -> {
                    timestamps.set(i, ANSI_BLUE + "[" + timestamps.get(i) + "] " + ANSI_RESET);
                });

        return timestamps;
    }
}
