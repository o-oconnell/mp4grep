package Arguments;

public class SearchArguments {
    public String search;

    SearchArguments(String search) {
        this.search = search;
    }

    public static SearchArgumentsBuilder builder() {
        return new SearchArgumentsBuilder();
    }

    public static class SearchArgumentsBuilder {
        private String search;

        SearchArgumentsBuilder() {
        }

        public SearchArgumentsBuilder search(String search) {
            this.search = search;
            return this;
        }

        public SearchArguments build() {
            return new SearchArguments(search);
        }

        public String toString() {
            return "SearchArguments.SearchArgumentsBuilder(search=" + this.search + ")";
        }
    }
}
