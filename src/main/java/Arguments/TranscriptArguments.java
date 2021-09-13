package Arguments;

import Transcription.VoskAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class TranscriptArguments {
    public List<String> files;
    public String search;
    public VoskAdapter speechToText;
}
