package Arguments;

import lombok.Builder;

@Builder
public class RawPrintArguments {
    public boolean printToFiles;
    public int wordsPerLine;
}
