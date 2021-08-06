import SpeechToText.VoskConverter;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoskConverterTest {

    private final static String RESOURCE_PATH = "src/test/resources/";

    @Test
    void VoskMP4ToWAVCreatesWAV() {
        String testFilename = getResourceFileName("test_wav_convert.mp4");
        String expectedResultFilename = getResourceFileName("test_wav_convert.wav");
        File resultFile = new File(expectedResultFilename);

        if (resultFile.exists())
            resultFile.delete();

        VoskConverter.convertToVoskFormat(testFilename);

        assertTrue(resultFile.exists(), "Missing wav file result from mp4 to wav conversion test.");
    }

    private String getResourceFileName(String filename) {
        return RESOURCE_PATH + filename;
    }

}
