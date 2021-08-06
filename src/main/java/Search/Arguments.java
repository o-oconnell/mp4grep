package Search;

import java.io.File;

public class Arguments {

    private String timestampFilename;
    private File contentFile;
    private int nWordsPrior;
    private int nWordsAfter;

    public Arguments(String timestampFilename,
                     String contentFilename,
                     int nWordsPrior,
                     int nWordsAfter) {

        this.timestampFilename = timestampFilename;
        this.contentFile = new File(contentFilename);
        this.nWordsAfter = nWordsAfter;
        this.nWordsPrior = nWordsPrior;
    }
}
