package Search;

import SpeechToText.SpeechToText;

import java.util.List;
import java.util.stream.Collectors;

public class Grepper {

    private final SpeechToText speechToText;
    private final String searchString;
    private final boolean isRegexSearch;
    List<String> inputFilesDirs; // input files and directories (directories are searched non-recursively by this grepper)

    // Defaulting to SpeechToText.VoskSpeechToText since no other java libs exist for sound transcription
    private Grepper(Grepper.GrepperBuilder builder) {
        this.speechToText = builder.speechToText;
        this.searchString = builder.searchString;
        this.isRegexSearch = builder.isRegexSearch;
        this.inputFilesDirs = builder.inputFilesDirs;
    }

    public void execute() {
     //   List<String> inputFiles = null;
        // TODO: steps to do next are: implement this with one sound engine and one type of grepper and one language
        // Then implement cache,
        // then implement searching,
        // then implement printing
        // then do other shit...


        // then make sure config exists in the program so can use it
        // and make sure ability to --cache (transcribe files without grepping)

        // (skip recursion, but do allow multiple files initially)
// low priority, later is to make it so users can edit the config


//
//        // Use the toSet function if necessary to avoid duplicate values coming from the input (prevent race conditions)
//        inputFiles.parallelStream()
//                .distinct() // (RATHER THAN ToSET: does the same thing as toset, filters out duplicate objects)
//                .map(speechToText::getGreppableResult) // maps each string to a greppable
//                .forEach(this::searchUsingGreppable); // searches each greppable

        // better practice to use this::(function name) <- (function pointer syntax)
        // - can read this whole execution procedure in a functional manner
        // (for each mapping we do this, then we do this to the previous thing, and so on).

        // TODO: the print operation should be separate from the search method, so the last forEach in the future
        // should print out the result of the grepping

        // TODO: find the documentation on streams and memorize all of the methods because goddamn
        // main things to learn: MAPS, FILTERS (filters out any objs for which a function returns false), FOREACH (end stream with, performs some final operation on everything)
        // COLLECT (end stream with, turns result into some output data structure)

        // TODO: learn about types of streams (e.g. intstreams, with an index)
        // Three main types of stream thing:
        // Things that can start streams (e.g. instream),
        // Things that can modify streams (e.g. maps)
        // Things that can end a stream (e.g. collect(), foreach())

        // TODO: cache key
        // should be a private object of VoskSpeechToText
        // TODO: cache
        // cache should be a wrapper around the sound engines
        // (Maybe) use HashCodeBuilder to generate a hash from the member variables of the cache key (object)
        // (this class guarantees that if the variables passed to it differ, then the hash will differ from the hash of other objects)


        // TODO: ---------------------------------HERE
        // TODO:
        // TODO:
        // TODO:
        // TODO: grepper should call the cache, and the cache should fall through to the sound engine
        // TODO: CACHE KEY IS A PRIVATE CLASS OF THE CACHE, CACHE KEY is a wrapper around each sound engine, so should contain
        // whatever is strictly reqiured to make a call with the sound engine (so just language and filename and sound engine type


        // TODO: make sure no wrinkles in the cache by ensuring that cache key member variables will be uniquely
        // identifiable across runs (e.g. do not use the object ID of a sound engine)
        // Two options are:
        // if the sound engine has member variables that identify it, then override the equals method and the hashcode method for the soundengine
        // otherwise, if any instance of a soundengine class will produce the same output given the same input, then you can just check that the class is the same
        // Everything needs to be either reduced to comparing if the class matches or comparing primitives that are members of the class


        // TODO: read the java documentation for the object class, because the object class
        // makes assumptions about the ways that the HashCode(), toString(), and equals() functions work
        // (because the hashcode returns a unique int, you can just have the toString() method return the unique integer as a string (return Integer.toString(this.HashCode()))

        GrepCacheWrapper cache = new GrepCacheWrapper(speechToText);

        if (!inputFilesDirs.isEmpty()) {
            for (String file : inputFilesDirs) { // TODO: this object will have the responsibility of getting the files to pass to the cache, and searching in directories
                // TODO: therefore, this call to the cache should only be provided with audio/video files
                cache.search(file, searchString);
            }
        }
    }

    // TODO: implement regex search for greppable objects
    private void searchUsingGreppable(Greppable grep) {

        if (isRegexSearch == true) {
            // grep.regexSearch(searchString);
            System.out.println("Regex search not yet supported");
        } else {
            grep.search(searchString);
        }
    }

    public static class GrepperBuilder {

        private SpeechToText speechToText;
        private String searchString;
        private boolean isRegexSearch;
        private List<String> inputFilesDirs;

        public GrepperBuilder() {}

        public GrepperBuilder speechToText(SpeechToText speechToText) {
            this.speechToText = speechToText;
            return this;
        }

        public GrepperBuilder searchString(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public GrepperBuilder isRegexSearch(boolean isRegexSearch) {
            this.isRegexSearch = isRegexSearch;
            return this;
        }

        public GrepperBuilder inputFilesDirs(List<String> inputFilesDirs) {
            this.inputFilesDirs = inputFilesDirs;
            return this;
        }

        public Grepper build() {
            Grepper grep = new Grepper(this);
            return grep;
        }
    }
}
