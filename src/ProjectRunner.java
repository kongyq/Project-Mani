import adapter.TestAdapter;
import core.Core;
import corenlp.CoreNlp;
import reader.DocReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 9/15/17.
 */
public class ProjectRunner {

    private final static File TEST = new File("/home/mike/Documents/corpus/orig/cv/1.txt");

    private TestAdapter adapter;

    public ProjectRunner(){
        this.adapter = null;

    }
    public static void main(String[] args) throws IOException {


        DocReader dr1 = new DocReader();
        DocReader dr2 = new DocReader();
        dr1.read(new File("/home/mike/Documents/corpus/orig/nklm/1.txt"));
        dr2.read(new File("/home/mike/Documents/corpus/orig/tse/6.txt"));

        CoreNlp coreNlp1 = new CoreNlp();
        CoreNlp coreNlp2 = new CoreNlp();

        coreNlp1.read(dr1.getText());
        coreNlp1.createMani();
//        System.out.println(coreNlp1.getMani());

        coreNlp2.read(dr2.getText());
        coreNlp2.createMani();

        Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());
        System.out.println(core.getSimilarity());
        System.out.println(core.getCommonWords());
        System.out.println(core.getJaccardSimilarity());
        System.out.println(core.getSimilarity() * core.getJaccardSimilarity());

    }
}
