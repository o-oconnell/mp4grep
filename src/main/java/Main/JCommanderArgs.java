package Main;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class JCommanderArgs {

        // Class used solely to store JCommander data

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--regex", "-r"}, description = "Flag to toggle regex searching")
    private boolean regex = false;

    @Parameter(names = {"--language", "-l"}, description = "Set the audio transcription language")
    private String language;

    @Parameter(names = {"--recursive", "-R"}, description = "Search recursively inside of a provided directory.")
    private boolean recursive;

    @Parameter(names = {"--search", "-s"}, description = "The search string", required = true)
    private String searchString;

    @Parameter(names = {"--timestamp", "-t"}, description = "The length of each timestamp in seconds")
    private Integer timestampSeconds;

    @Parameter(names = {"--count", "-c"}, description = "Output a count of matching words/phrases only")
    private boolean count;

    @Parameter(names = {"--input", "-i"}, description = "An audio/video file or directory containing audio/video files", required = true)
    private String input;

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Integer getTimestampSeconds() {
        return timestampSeconds;
    }

    public void setTimestampSeconds(Integer timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
