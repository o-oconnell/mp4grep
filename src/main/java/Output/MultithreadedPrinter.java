package Output;

import java.util.ArrayList;

public class MultithreadedPrinter {

    private static ArrayList<ArrayList<String>> lineSets = new ArrayList<ArrayList<String>>();
    private static Object linesLock = new Object();

    public static void add(ArrayList<String> list) {
        synchronized (linesLock) {
            lineSets.add(list);
        }
    }

    public static void print() {
        for (ArrayList<String> set : lineSets) {
            for (String line : set) {
                System.out.println(line);
            }
        }
    }
}
