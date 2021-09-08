package Arguments;

public class GrepperArguments {
    public TranscriptArguments transcriptArguments;
    public SearchArguments searchArguments;
    public PrintArguments printArguments;

    GrepperArguments(TranscriptArguments transcriptArguments, SearchArguments searchArguments, PrintArguments printArguments) {
        this.transcriptArguments = transcriptArguments;
        this.searchArguments = searchArguments;
        this.printArguments = printArguments;
    }

    public static GrepperArgumentsBuilder builder() {
        return new GrepperArgumentsBuilder();
    }

    public static class GrepperArgumentsBuilder {
        private TranscriptArguments transcriptArguments;
        private SearchArguments searchArguments;
        private PrintArguments printArguments;

        GrepperArgumentsBuilder() {
        }

        public GrepperArgumentsBuilder transcriptArguments(TranscriptArguments transcriptArguments) {
            this.transcriptArguments = transcriptArguments;
            return this;
        }

        public GrepperArgumentsBuilder searchArguments(SearchArguments searchArguments) {
            this.searchArguments = searchArguments;
            return this;
        }

        public GrepperArgumentsBuilder printArguments(PrintArguments printArguments) {
            this.printArguments = printArguments;
            return this;
        }

        public GrepperArguments build() {
            return new GrepperArguments(transcriptArguments, searchArguments, printArguments);
        }

        public String toString() {
            return "GrepperArguments.GrepperArgumentsBuilder(transcriptArguments=" + this.transcriptArguments + ", searchArguments=" + this.searchArguments + ", printArguments=" + this.printArguments + ")";
        }
    }
}
