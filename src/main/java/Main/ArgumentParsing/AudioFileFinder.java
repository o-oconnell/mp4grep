package Main.ArgumentParsing;

import Globals.GlobalColors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AudioFileFinder {
    private static final List<String> VALID_FILE_EXTENSIONS = List.of(
            "mp4",
            "ogg",
            "webm",
            "mp2",
            "mp3",
            "mov",
            "wav");
    private static final Locale LOCAL_LOCALE = Locale.ROOT;

    public static List<String> getFileList(List<String> filesAndDirectories) {
        List<String> files = new LinkedList<>();
        for (String input : filesAndDirectories) {
            if (isDirectory(input)) {
                List<String> filesFromDirectory = getAudioFilesFromDirectory(input);
                files.addAll(filesFromDirectory);
            } else if (isValidAudioFile(input)) {
                files.add(input);
            } else {
                System.out.println(GlobalColors.ANSI_RED + "Location \"" + input + "\" not found or invalid audio file." + GlobalColors.ANSI_RESET);
            }
        }
        return files;
    }

    private static List<String> getAudioFilesFromDirectory(String directoryName) {
        File directory = new File(directoryName);
        File[] allFiles = directory.listFiles();

        List<String> result = new LinkedList<>();
        for (File file : allFiles) {
            if (isAudioFile(file.getName()))
                result.add(file.getAbsolutePath());
        }
        return result;
    }

    private static boolean isDirectory(String input) {
        File file = new File(input);
        return file.isDirectory();
    }

    private static boolean isValidAudioFile(String input) {
        Path inputPath = Paths.get(input);
        return Files.exists(inputPath) && isAudioFile(input);
    }

    private static boolean isAudioFile(String file) {
        String fileExtension = getFileExtension(file);

        for (String extension: VALID_FILE_EXTENSIONS) {
            if (fileExtension.equals(extension))
                return true;
        }

        return false;
    }

    private static String getFileExtension(String filename) {
        int lastPeriodIndex = filename.lastIndexOf(".");
        return filename
                .substring(lastPeriodIndex + 1)
                .toLowerCase(LOCAL_LOCALE);
    }
}
