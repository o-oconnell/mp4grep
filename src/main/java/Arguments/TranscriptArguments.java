package Arguments;

import Transcribe.VoskAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class TranscriptArguments {
    public List<String> files;
    public VoskAdapter speechToText;
    public String modelDirectory;
}
