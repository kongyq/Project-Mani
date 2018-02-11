import core.Core;
import corenlp.CoreNlp;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.xml.sax.SAXException;
import reader.DocReader;
import tfidf.CorpusParser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by mike on 11/1/17.
 */
public class WorkerThread2 implements Callable<Double[]> {

    private File doc1;
    private File doc2;
    private CorpusParser parser;

    public WorkerThread2(File doc1, File doc2, CorpusParser parser){
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.parser = parser;
    }

    public Double[] call() throws Exception{

        return maniDoc2Doc(this.doc1, this.doc2, this.parser);

    }

    public File getTxtName(File htmlFile){
        return new File(htmlFile.toString().replaceFirst("TmpCorpus", "TxtCorpus").replaceFirst("html", "txt"));
    }

    public Double[] maniDoc2Doc (File doc1, File doc2, CorpusParser parser) throws IOException, BoilerpipeProcessingException, SAXException {
        DocReader dr1 = new DocReader();
        DocReader dr2 = new DocReader();
        dr1.readHtml(doc1);
        dr2.readHtml(doc2);

        CoreNlp coreNlp1 = new CoreNlp();
        CoreNlp coreNlp2 = new CoreNlp();
        coreNlp1.read(dr1.getText());
        coreNlp2.read(dr2.getText());
        coreNlp1.createMani();
        coreNlp2.createMani();

        Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());
        Double tfidf = parser.getCosineSimilarity(getTxtName(doc1), getTxtName(doc2));

        return new Double[]{tfidf, //TFIDF
                core.getSimilarity(),                                                       //Mani
                core.getJaccardSimilarity(),                                                //Ratio
                core.getSimilarity() * core.getJaccardSimilarity(),
                tfidf * core.getSimilarity()};                        //Product

    }

}
