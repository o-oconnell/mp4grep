package Arguments;

import lombok.Builder;

@Builder
public class PrintArguments {
    public String search;
    public int wordsBeforeMatch;
    public int wordsAfterMatch;
}
