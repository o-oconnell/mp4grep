public class MP4Grep {

    String audioFile;

    // Defaulting to VoskSoundEngineAdapter since no other java libs exist for sound transcription
    SoundEngineAdapter soundAdapter = new VoskSoundEngineAdapter();
    Grepper grepper = new Grepper();

    public MP4Grep() {

    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public void search(String searchString) {
        soundAdapter.printAudioTranscriptionToFile(audioFile);
        grepper.setTempFile(soundAdapter.getTempAudioStorageFile());
        grepper.search(searchString);
        grepper.clearTempFile();
    }
}
