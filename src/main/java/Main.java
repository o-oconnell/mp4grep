import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {


    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {

        VoskSoundEngineAdapter voskadapter = new VoskSoundEngineAdapter();
        voskadapter.parseAudioFile("python_example_test.wav");

    }
}
