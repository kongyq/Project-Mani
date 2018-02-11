import core.Core;
import corenlp.CoreNlp;
import reader.DocReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 11/1/17.
 */
public class WorkerThread implements Runnable {

    private File doc1;
    private File doc2;

    public WorkerThread(File doc1, File doc2){
        this.doc1 = doc1;
        this.doc2 = doc2;
    }

    public void run(){
        try {
            maniDoc2Doc(this.doc1, this.doc2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void maniDoc2Doc (File doc1, File doc2) throws IOException {
        DocReader dr1 = new DocReader();
        DocReader dr2 = new DocReader();
        dr1.read(doc1);
        dr2.read(doc2);

        CoreNlp coreNlp1 = new CoreNlp();
        CoreNlp coreNlp2 = new CoreNlp();
        coreNlp1.read(dr1.getText());
        coreNlp2.read(dr2.getText());
        coreNlp1.createMani();
        coreNlp2.createMani();

        Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());

        System.out.format("%10s %10s %9s %9s ManiScore: %.6f\tJaccard: %.6f\tProduct: %.6f \n",
                doc1.getParentFile().getName(),
                doc2.getParentFile().getName(),
                doc1.getName(),
                doc2.getName(),
                core.getSimilarity(),
                core.getJaccardSimilarity(),
                core.getSimilarity() * core.getJaccardSimilarity());

    }

}
