import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {


    public static void main(String[] args) {

        MP4Grep grep = new MP4Grep();
        grep.setAudioFile("python_example_test.wav");
        grep.search("one");

    }
}
