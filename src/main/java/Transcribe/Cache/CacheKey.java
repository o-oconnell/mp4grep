package Transcribe.Cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Globals.GlobalLocations.CACHE_DIRECTORY;

public class CacheKey {
    private static final String TRANSCRIPT_FILE_EXTENSION = ".transcript";
    private static final String TIMESTAMP_FILE_EXTENSION = ".timestamp";
    private static final int MINIMUM_HASH_BITS = 64;

    public String filename;
    public String lastModified;
    public String modelDirectory;
    private String hash;

    public CacheKey(String filename, String modelDirectory) {
        createCacheDirectory();
        this.filename = filename;
        this.lastModified = getLastModified(filename);
        this.hash = computeHash();
        this.modelDirectory = modelDirectory;
    }

    private void createCacheDirectory() {
        try {
            Files.createDirectories(Paths.get(CACHE_DIRECTORY));
        } catch (IOException e) {
            System.out.println("Failed to create cache directory \"" + CACHE_DIRECTORY + "\"");
            e.printStackTrace();
        }
    }

    private String getLastModified(String filename) {
        File file = new File(filename);
        return String.valueOf(file.lastModified());
    }

    private String computeHash() {
        byte[] fileData = getFileData(filename);

        HashFunction hashFunction = Hashing.goodFastHash(MINIMUM_HASH_BITS);
        HashCode hashCode = hashFunction.newHasher()
                .putBytes(fileData)
                .hash();

        return hashCode.toString();
    }

    private byte[] getFileData(String filename) {
        Path audioFile = Paths.get(filename);

        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(audioFile);
        } catch (IOException e) {
            System.out.println("Failed to read file for checksum computation.");
            e.printStackTrace();
        }
        return fileData;
    }

    public String getTranscriptFilename() {
        return CACHE_DIRECTORY + "/" + this.hash + TRANSCRIPT_FILE_EXTENSION;
    }

    public String getTimestampFilename() {
        return CACHE_DIRECTORY + "/" + this.hash + TIMESTAMP_FILE_EXTENSION;
    }

    public String getModelDirectory() {
        return this.modelDirectory;
    }
}
