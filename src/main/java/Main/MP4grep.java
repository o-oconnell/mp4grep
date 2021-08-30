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

        ArgumentParser parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        grepper.execute();
    }
}
