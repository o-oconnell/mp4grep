

public class Main {
    //ArgumentParser parser = new ArgumentParser(); // put this in constructor instead
    // also could maybe make ArgumentParser static since it won't have any state.

    public static void main(String[] args) {
       // Grepper grepper = parser.getGrepperForArgs(args);

        // runs whatever search was created by parser.
     //   grepper.execute(); // possibly throws exceptions that could be handled up here.


        // new class hierarchy:
        // MP4Grep with main, getting args, handling exceptions (lowest level of the program
        // where devs will have enough context to do something about the exception)
        // -> Grepper that is built with objects (by the command line parser) based on the arguments.
        // Grepper depends on SpeechToText, Greppable, and the new objects from the command line parser to

        // NOTE: grepper is an abstract class (containing shared fields that every type of grepper will have)
        // child classes of the grepper may in future implement features that will require significant modifications
        // to the grepper, such as recursive grepping

        // NOTE: grepper will be an interface or abstract class LATER afte the primary basic functionality
        // of the initial grepper is designed.


        SpeechToText soundAdapter = new VoskSpeechToText("OSR_us_000_0010_8k.wav");

        MP4Grep grep = new MP4Grep.MP4GrepBuilder()
                .soundAdapter(soundAdapter)
                .build();

        grep.search("person");

        // FUTURE CONTENTS OF MP4GREP:
        ArgumentParser parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        grepper.execute();
    }
}
