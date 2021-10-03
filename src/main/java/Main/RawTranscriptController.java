package Main;

import Print.TranscribePrint.TranscribePrintAdapter;
import Search.Searchable;
import Transcribe.TranscriptAdapter;
import lombok.Builder;

import java.util.List;

@Builder
public class RawTranscriptController implements Controller {
    private TranscriptAdapter transcriptAdapter;
    private TranscribePrintAdapter rawPrintAdapter;

    @Override
    public void execute() {
        List<Searchable> searchables = transcriptAdapter.getSearchables();
        rawPrintAdapter.print(searchables);
    }
}
