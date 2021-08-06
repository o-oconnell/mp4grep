package Main;

import Search.Grepper;

public class MP4grep {

    public static void main(String[] args) {

        // TODO: create a standard formatter that does all of the printing for this project
        // It takes strings or json from each sound engine and formats them to print,
        // which allows us to easily modify the output and prevents us from having the SRP violated by the adapter classes to the
        // sound engines.

        // NOTE: each adapter class should be solely responsible for translating input into a format/action understandable
        // by the sound engine and translating back the result of that to the program.


        // TODO: Rather than deleting and recreating a single cache file, create new cache files for new searches
        // CACHE KEY includes: sound engine, most recent time of modification for the file, the filename, the language
        // (all of the information that would change the TRANSCRIPTION of the file).

        // How the cache will work: hash an object containing member variables that comprise all of the information that uniquely
        // identifies an audio transcription. Will need to override hashcode method.

        ArgumentParser parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        grepper.execute();
    }
}
