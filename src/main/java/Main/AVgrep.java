package Main;

public class AVgrep {
    public static void main(String[] args) {
        ArgumentParser parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        grepper.execute();
    }
}
