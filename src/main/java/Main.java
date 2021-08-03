

public class Main {

    public static void main(String[] args) {

        SoundEngineAdapter soundAdapter = new VoskSoundEngineAdapter("OSR_us_000_0010_8k.wav");

        MP4Grep grep = new MP4Grep.MP4GrepBuilder()
                .soundAdapter(soundAdapter)
                .build();

        grep.search("person");
    }
}
