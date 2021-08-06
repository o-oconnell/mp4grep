public class Grepper {

    private SpeechToText speechToText;
    private String searchString;

    // Defaulting to VoskSpeechToText since no other java libs exist for sound transcription
    private Grepper(Grepper.GrepperBuilder builder) {
        this.speechToText = builder.speechToText;
        this.searchString = builder.searchString;
    }

    public void execute() {

        Greppable grep = speechToText.getGreppableResult();
        grep.search(searchString);
    }

    public static class GrepperBuilder {

        private SpeechToText speechToText;
        private String searchString;

        public GrepperBuilder() {}

        public GrepperBuilder speechToText(SpeechToText speechToText) {
            this.speechToText = speechToText;
            return this;
        }

        public GrepperBuilder searchString(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public Grepper build() {
            Grepper grep = new Grepper(this);
            return grep;
        }

    }
}
