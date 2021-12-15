package Transcribe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class VoskConverterTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void convertToVoskFormat() {
        String convertedFilename = VoskConverter.convertToVoskFormat("harvardsentences.mp4");
        File convertedFile = new File(convertedFilename);
        assertTrue(convertedFile.exists());
    }

    @Test
    void getAudioDuration() {
    }

    @Test
    void getConvertedFilename() {
    }
}