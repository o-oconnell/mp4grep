package Print.TranscribePrint;

import Arguments.RawPrintArguments;
import Globals.GlobalColors;
import Search.Searchable;
import Search.Searcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TranscribePrinter {
    private static String TRANSCRIBED_FILE_EXTENSION = "_transcribed.txt";
    private static int ERROR_EXIT_CODE = 1;
    private RawPrintArguments arguments;

    public TranscribePrinter(RawPrintArguments arguments) {
        this.arguments = arguments;
    }

    public void print(Searchable searchable) {
        List<String> printList = new ArrayList<>();
        Deque<String> words = new LinkedList<>(Arrays.asList(Searcher.getContentsWithoutNewlines(searchable.transcriptFile).split(" ")));
        Deque<String> timestamps = new LinkedList<>(Arrays.asList(Searcher.getContentsWithoutNewlines(searchable.timestampFile).split(" ")));

        while (!words.isEmpty()) {
            String current = "[" + timestamps.getFirst() + "]";
            int i = 0;
            for (; i < arguments.wordsPerLine; ++i) {
                if (!words.isEmpty()) {
                    current += " " + words.removeFirst();
                }
            }
            popFirstN(i, timestamps);
            printList.add(current);
        }

        if (arguments.printToFiles == true) {
            printToFile(printList, searchable.filename);
        } else {
            System.out.println(GlobalColors.ANSI_GREEN + "Transcription of " + searchable.filename + GlobalColors.ANSI_RESET);
            printList.stream().forEach(System.out::println);
        }
    }

    public void popFirstN(int n, Deque<String> deque) {
        for (int i = 0; i < n; ++i) {
            if (!deque.isEmpty())
                deque.pop();
        }
    }

    private void printToFile(List<String> list, String filename) {
        if (fileExists(getOutFilename(filename))) {
            System.out.println("Error: file " + getOutFilename(filename) + " already exists");
            return;
        }
        list.stream()
                .forEach(string -> printToFile(string, filename));
    }

    private String getOutFilename(String filename) {
        return filename + TRANSCRIBED_FILE_EXTENSION;
    }

    private void printToFile(String string, String filename) {
        String outFilename = getOutFilename(filename);
        createNewFile(outFilename);
        Path path = Path.of(outFilename);
        try {
            Files.writeString(path, string + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to write transcription to file.");
            e.printStackTrace();
        }
    }

    private boolean fileExists(String outFilename) {
        Path path = Path.of(outFilename);
        if (Files.exists(path)) {
            return true;
        }
        return false;
    }

    private void createNewFile(String filename) {
        try {
            File file = new File(filename);
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating transcript file.");
            e.printStackTrace();
        }
    }
}
