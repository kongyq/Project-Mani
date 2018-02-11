import core.Core;
import core.CoreMani;
import corenlp.CoreNlp;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.stanford.nlp.util.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.SAXException;
import reader.DocReader;
import tfidf.CorpusParser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by mike on 11/1/17.
 */
public class CoreThread implements Callable<Pair<Pair<Integer, Integer>, Double[]>> {

    private CoreNlp doc1;
    private CoreNlp doc2;
    private Double[] thresholds;
    private Pair<Integer,Integer> docPair;

    public CoreThread(Pair<Integer,Integer> docPair, CoreNlp doc1, CoreNlp doc2, Double[] thresholds){
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.docPair = docPair;
        this.thresholds = thresholds;
    }


    public Pair<Pair<Integer, Integer>, Double[]> call() throws Exception{
        return new Pair<>(this.docPair,maniDoc2Doc(this.doc1, this.doc2, this.thresholds));
    }

    public Double[] maniDoc2Doc (CoreNlp doc1, CoreNlp doc2, Double[] thresholds){

        doc1.createMani();
        doc2.createMani();
        doc1.createSentenceList();
        doc2.createSentenceList();

//        Core core = new Core(doc1.getMani(), doc2.getMani());
        CoreMani coreMani = new CoreMani(doc1.getSentenceList(), doc2.getSentenceList());

//        Double jaccard = core.getJaccardSimilarity();

        coreMani.run(new POS[] {POS.n, POS.v}, true);

        //For jaccard TEST only
        Double jaccard = coreMani.getJaccardSimilarity();

        Double[] maniScores = ArrayUtils.toObject(Arrays.stream(thresholds).
                mapToDouble(threshold -> coreMani.getManiScore_threshold(threshold)).
                toArray());

        //For jaccard TEST only
        maniScores[maniScores.length-1] = jaccard;

        return maniScores;
    }

}
