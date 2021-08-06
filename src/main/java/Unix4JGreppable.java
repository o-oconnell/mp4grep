import org.unix4j.Unix4j;
import org.unix4j.line.Line;

import java.io.File;
import java.util.List;

public class Unix4JGreppable implements Greppable {

    String filename;

    public Unix4JGreppable(String filename) {
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
