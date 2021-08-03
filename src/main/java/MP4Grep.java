public class MP4Grep {

    private SoundEngineAdapter soundAdapter;

    // Defaulting to VoskSoundEngineAdapter since no other java libs exist for sound transcription
    private MP4Grep(MP4GrepBuilder builder) {
        this.soundAdapter = builder.soundAdapter;
    }

    public void search(String searchString) {

        Greppable grep = soundAdapter.getGreppableResult();
        grep.search(searchString);
    }

    public static class MP4GrepBuilder {

        private SoundEngineAdapter soundAdapter;

        public MP4GrepBuilder() {}

        public MP4GrepBuilder soundAdapter(SoundEngineAdapter soundAdapter) {
            this.soundAdapter = soundAdapter;
            return this;
        }

        public MP4Grep build() {
            MP4Grep grep = new MP4Grep(this);
            return grep;
        }

    }
}
