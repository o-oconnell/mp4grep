package Arguments;

public class PrintArguments {
    public String search;
    public int wordsBeforeMatch;
    public int wordsAfterMatch;

    PrintArguments(String search, int wordsBeforeMatch, int wordsAfterMatch) {
        this.search = search;
        this.wordsBeforeMatch = wordsBeforeMatch;
        this.wordsAfterMatch = wordsAfterMatch;
    }

    public static PrintArgumentsBuilder builder() {
        return new PrintArgumentsBuilder();
    }

    public static class PrintArgumentsBuilder {
        private String search;
        private int wordsBeforeMatch;
        private int wordsAfterMatch;

        PrintArgumentsBuilder() {
        }

        public PrintArgumentsBuilder search(String search) {
            this.search = search;
            return this;
        }

        public PrintArgumentsBuilder wordsBeforeMatch(int wordsBeforeMatch) {
            this.wordsBeforeMatch = wordsBeforeMatch;
            return this;
        }

        public PrintArgumentsBuilder wordsAfterMatch(int wordsAfterMatch) {
            this.wordsAfterMatch = wordsAfterMatch;
            return this;
        }

        public PrintArguments build() {
            return new PrintArguments(search, wordsBeforeMatch, wordsAfterMatch);
        }

        public String toString() {
            return "PrintArguments.PrintArgumentsBuilder(search=" + this.search + ", wordsBeforeMatch=" + this.wordsBeforeMatch + ", wordsAfterMatch=" + this.wordsAfterMatch + ")";
        }
    }
}
