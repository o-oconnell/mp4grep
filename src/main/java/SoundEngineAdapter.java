import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface SoundEngineAdapter {

    public default void printAudioTranscriptionToFile(String audioFileName) {}

    public String getTempAudioStorageFile();
    public void setTempAudioStorageFile(String tempAudioStorageFile);


}
