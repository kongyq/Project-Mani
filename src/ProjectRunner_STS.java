import com.google.common.base.Stopwatch;
import reader.DocReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 3/1/18.
 */
public class ProjectRunner_STS {

    private static final File DATASET = new File("/home/mike/Documents/corpus/stsbenchmark/sts-test.csv");

    public static void main (String[] args) throws IOException {

        Stopwatch timer = Stopwatch.createStarted();
        DocReader docReader = new DocReader();
        docReader.readShortArticle(DATASET);

    }
}
