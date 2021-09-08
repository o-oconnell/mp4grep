import org.junit.jupiter.api.Test;
import Search.GreppableTranscript;

public class GreppableTest {
    private final static String RESOURCE_PATH = "src/test/resources/";

    @Test
    public void testMatching() {

        String wordsFilename = getResourceFileName("testMatching1.txt");
        String timestampsFilename = getResourceFileName("testMatchingTimestamps.txt");
        int nWordsPrior = 1;
        int nWordsAfter = 1;

        GreppableTranscript grep = new GreppableTranscript(timestampsFilename, wordsFilename, nWordsPrior, nWordsAfter);

        grep.search("architectural");
    }

    private String getResourceFileName(String filename) {
        return RESOURCE_PATH + filename;
    }


}
