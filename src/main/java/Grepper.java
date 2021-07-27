import org.unix4j.line.Line;
import org.unix4j.unix.Grep;
import org.unix4j.Unix4j;

import java.io.File;
import java.util.List;


public class Grepper {

    private String tempFile;

    public void setTempFile(String tempFile) {
        this.tempFile = tempFile;
    }

    public void search(String searchString) {
        File file = new File(tempFile);
        List<Line> lines = Unix4j.grep(searchString, file).toLineList();

        for (Line l : lines) {
            System.out.println(l.getContent());
        }
    }

    public void clearTempFile() {
        File file = new File(tempFile);
        file.delete();
    }

}
