package Arguments;

public class SpeechToTextArguments {

    public int wordsToPrintBeforeMatch = 5;
    public int wordsToPrintAfterMatch = 5;

    SpeechToTextArguments(int wordsToPrintBeforeMatch, int wordsToPrintAfterMatch) {
        this.wordsToPrintBeforeMatch = wordsToPrintBeforeMatch;
        this.wordsToPrintAfterMatch = wordsToPrintAfterMatch;
    }

    private static int $default$wordsToPrintBeforeMatch() {
        return 5;
    }

    private static int $default$wordsToPrintAfterMatch() {
        return 5;
    }

    public static SpeechToTextArgumentsBuilder builder() {
        return new SpeechToTextArgumentsBuilder();
    }

    public static class SpeechToTextArgumentsBuilder {
        private int wordsToPrintBeforeMatch$value;
        private boolean wordsToPrintBeforeMatch$set;
        private int wordsToPrintAfterMatch$value;
        private boolean wordsToPrintAfterMatch$set;

        SpeechToTextArgumentsBuilder() {
        }

        public SpeechToTextArgumentsBuilder wordsToPrintBeforeMatch(int wordsToPrintBeforeMatch) {
            this.wordsToPrintBeforeMatch$value = wordsToPrintBeforeMatch;
            this.wordsToPrintBeforeMatch$set = true;
            return this;
        }

        public SpeechToTextArgumentsBuilder wordsToPrintAfterMatch(int wordsToPrintAfterMatch) {
            this.wordsToPrintAfterMatch$value = wordsToPrintAfterMatch;
            this.wordsToPrintAfterMatch$set = true;
            return this;
        }

        public SpeechToTextArguments build() {
            int wordsToPrintBeforeMatch$value = this.wordsToPrintBeforeMatch$value;
            if (!this.wordsToPrintBeforeMatch$set) {
                wordsToPrintBeforeMatch$value = SpeechToTextArguments.$default$wordsToPrintBeforeMatch();
            }
            int wordsToPrintAfterMatch$value = this.wordsToPrintAfterMatch$value;
            if (!this.wordsToPrintAfterMatch$set) {
                wordsToPrintAfterMatch$value = SpeechToTextArguments.$default$wordsToPrintAfterMatch();
            }
            return new SpeechToTextArguments(wordsToPrintBeforeMatch$value, wordsToPrintAfterMatch$value);
        }

        public String toString() {
            return "SpeechToTextArguments.SpeechToTextArgumentsBuilder(wordsToPrintBeforeMatch$value=" + this.wordsToPrintBeforeMatch$value + ", wordsToPrintAfterMatch$value=" + this.wordsToPrintAfterMatch$value + ")";
        }
    }
}
