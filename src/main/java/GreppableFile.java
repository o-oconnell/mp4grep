import org.unix4j.Unix4j;
import org.unix4j.line.Line;

import java.io.File;
import java.util.List;

public class GreppableFile implements Greppable {

    String filename;

    public GreppableFile(String filename) {
        this.filename = filename;
    }

    @Override
    public void search(String searchString) {
        File file = new File(filename);
        List<Line> lines = Unix4j.grep(searchString, file).toLineList();

        for (Line l : lines) {
            System.out.println(l.getContent());
        }
    }
}
