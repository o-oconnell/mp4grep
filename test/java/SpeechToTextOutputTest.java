import Output.SpeechToTextOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpeechToTextOutputTest {

    @Test
    void testTimestampedStringConcatenation() {

        int maxTimestampTimeSeconds = 5;
        SpeechToTextOutput output = new SpeechToTextOutput(maxTimestampTimeSeconds);

        output.addTimestampedWordToOutput(0, 1, "string one");
        output.addTimestampedWordToOutput(1.1, 3.23, "String two");

        Assertions.assertEquals("0.0---3.23: string one String two", output.toStringArrayList().get(0));
    }

    @Test
    void testMultiLineStringOutput() {
        int maxTimestampTimeSeconds = 5;
        SpeechToTextOutput output = new SpeechToTextOutput(maxTimestampTimeSeconds);

        output.addTimestampedWordToOutput(0, 1, "string one");
        output.addTimestampedWordToOutput(1.1, 3.23, "String two");
        output.addTimestampedWordToOutput(3.4, 5.6, "String three (new line)");

        Assertions.assertEquals("0.0---3.23: string one String two", output.toStringArrayList().get(0));
        Assertions.assertEquals("3.4---5.6: String three (new line)", output.toStringArrayList().get(1));
    }
}
