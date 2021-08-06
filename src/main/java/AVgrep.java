import com.sun.jdi.connect.Connector;

public class AVgrep {


    private static ArgumentParser parser;

    public static void main(String[] args) {
        parser = new ArgumentParser();
        Grepper grepper = parser.getGrepperForArgs(args);
        //grepper.execute();
    }
}
