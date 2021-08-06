package Main;

import Search.Grepper;

public class MP4grep {

    public static void main(String[] args) {

        ArgumentParser parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        grepper.execute();
    }
}
