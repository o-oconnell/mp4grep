package Search;

import Arguments.GrepperArguments;
import Output.MultithreadedPrinter;
import SpeechToText.SpeechToText;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Grepper {

    private GrepperArguments arguments;
    private final List<String> VALID_FILE_EXTENSIONS = List.of(
                    "mp4",
                    "ogg",
                    "webm",
                    "mp2",
                    "mp3",
                    "mov",
                    "wav");
   // GrepCacheWrapper cache;

    public Grepper(GrepperArguments arguments) {
        this.arguments = arguments;
        // cache = new GrepCacheWrapper(arguments);
    }

    public void execute() {
        searchAllFilesAndDirectories(arguments.inputFilesDirs);
    }

    private void searchAllFilesAndDirectories(List<String> inputFilesDirs) {
        if (!inputFilesDirs.isEmpty()) {
            inputFilesDirs.parallelStream().forEach(this::callCacheForInput);
        }
        MultithreadedPrinter.print();
    }

    private void callCacheForInput(String input) {

        if (!isValidInput(input)) {
            System.out.println("File/directory \"" + input + "\" is invalid.");
            return;
        }

        if (isDirectory(input)) {
            List<String> files = getAudioFilesFromDirectory(input);
            searchAllFilesAndDirectories(files);
        } else {
            GrepCacheWrapper cache = new GrepCacheWrapper(arguments);
            cache.search(input, arguments.searchString);
        }
    }

    private boolean isValidInput(String input) {
        Path inputPath = Paths.get(input);
        return Files.exists(inputPath);
    }

    private boolean isDirectory(String input) {
        File file = new File(input);
        return file.isDirectory();
    }

    private List<String> getAudioFilesFromDirectory(String directoryName) {
        File directory = new File(directoryName);
        File[] allFiles = directory.listFiles();

        List<String> result = new LinkedList<String>();
        for (File file : allFiles) {
            if (isAudioFile(file))
                result.add(directoryName + "/" + file.getName());
        }
        return result;
    }

    private boolean isAudioFile(File file) {
        String fileExtension = getFileExtension(file);

        for (String extension: VALID_FILE_EXTENSIONS) {
            if (fileExtension.equals(extension))
                return true;
        }
        return false;
    }

    private String getFileExtension(File file) {
        String filename = file.getName();
        int lastPeriodIndex = filename.lastIndexOf(".");

        return filename
                .substring(lastPeriodIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    // TODO: implement regex search for greppable objects
    private void searchUsingGreppable(Greppable grep) {
        grep.search(arguments.searchString);
    }
}
