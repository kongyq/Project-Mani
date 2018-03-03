import com.google.common.base.Stopwatch;
import core.Core;
import core.CoreMani;
import corenlp.CoreNlp;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.stanford.math.plex4.homology.barcodes.BarcodeCollection;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;
import reader.DocReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 12/8/17.
 */
public class testCoreMani {

    public static void main(String[] args) throws IOException {
        DocReader dr1 = new DocReader();
        DocReader dr2 = new DocReader();
        dr1.read(new File("/home/mike/Documents/corpus/orig/cv/1.txt"));
        dr2.read(new File("/home/mike/Documents/corpus/orig/cv/2.txt"));

        Stopwatch timer = Stopwatch.createStarted();
        CoreNlp coreNlp1 = new CoreNlp();
        CoreNlp coreNlp2 = new CoreNlp();

        coreNlp1.read(dr1.getText());
        coreNlp1.createSentenceList();


//        coreNlp1.createMani();
//        System.out.println(coreNlp1.getMani());

        coreNlp2.read(dr2.getText());
//        coreNlp2.createMani();
        coreNlp2.createSentenceList();
//        timer.stop();
        System.out.println("CoreNLP: " + timer.stop());

        timer.reset();
        timer.start();
        CoreMani coreMani = new CoreMani(coreNlp1.getSentenceList(), coreNlp2.getSentenceList(), null, null);
        coreMani.run(new POS[]{POS.n, POS.v}, true);
        System.out.println("CoreMani:" + timer.stop());

        timer.reset();
        timer.start();
//        System.out.println(coreMani.getManiScore(3.0));
        System.out.println(coreMani.getManiScore_threshold(0D));
//        coreMani.getBarcode();
        System.out.println("Compute Barcode: " + timer);
  /*      System.out.format("%s%n %s%n",
                coreMani.getBarcode(),
                coreMani.getBarcode().getBettiNumbers());*/
//        System.out.println(coreMani.getIntervals(1));
//        coreMani.getIntervals(1);


//        System.out.println(coreNlp1.getSentenceList().getFirst());
//        System.out.println(CoreMani.getAllAdjacentNodePairs(coreNlp1.getSentenceList().getFirst()).size());
//        for (Pair<IndexedWord, IndexedWord> nodePair: CoreMani.getAllAdjacentNodePairs(coreNlp1.getSentenceList().getFirst())){
//            System.out.format("%d, %d%n",
//                    CoreMani.indexedwordToVertex(nodePair.first(),1),
//                    CoreMani.indexedwordToVertex(nodePair.second(), 1));
//                    nodePair.first().word(),
//                    nodePair.second().word());
        }

//        CoreMani coreMani = new CoreMani(coreNlp1.getSentenceList(), coreNlp2.getSentenceList());


//        Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());
//        System.out.println(core.getSimilarity());
//    }
}
