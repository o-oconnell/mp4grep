package Arguments;

import Transcription.VoskAdapter;

import java.util.List;

public class TranscriptArguments {
    public List<String> files;
    public String search;
    public VoskAdapter speechToText;

    TranscriptArguments(List<String> files, String search, VoskAdapter speechToText) {
        this.files = files;
        this.search = search;
        this.speechToText = speechToText;
    }

    public static TranscriptArgumentsBuilder builder() {
        return new TranscriptArgumentsBuilder();
    }

    public static class TranscriptArgumentsBuilder {
        private List<String> files;
        private String search;
        private VoskAdapter speechToText;

        TranscriptArgumentsBuilder() {
        }

        public TranscriptArgumentsBuilder files(List<String> files) {
            this.files = files;
            return this;
        }

        public TranscriptArgumentsBuilder search(String search) {
            this.search = search;
            return this;
        }

        public TranscriptArgumentsBuilder speechToText(VoskAdapter speechToText) {
            this.speechToText = speechToText;
            return this;
        }

        public TranscriptArguments build() {
            return new TranscriptArguments(files, search, speechToText);
        }

        public String toString() {
            return "TranscriptArguments.TranscriptArgumentsBuilder(files=" + this.files + ", search=" + this.search + ", speechToText=" + this.speechToText + ")";
        }
    }
}
