package Search;

import java.io.File;

public interface Greppable {

    void search(String searchString);
    void storeTranscriptInLocation(File transcriptFile);
    void storeTimestampsInLocation(File timestampFile);

}
