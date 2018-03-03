import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;
import wordnet.Adw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 2/27/18.
 */
public class WorkerThreadGO implements Runnable {

    private Map.Entry<IndexedWord, List<String>> entry1;
    private Map.Entry<IndexedWord, List<String>> entry2;
    private Adw adw;
    private Pair<Integer, Integer> docPair;
    private GenerateOracle oracle;

    public WorkerThreadGO(Pair<Integer,Integer> docPair, GenerateOracle oracle, Adw adw){
        this.adw = adw;
        this.docPair = docPair;
        this.oracle = oracle;
    }

    public WorkerThreadGO(Map.Entry<IndexedWord, List<String>> entry1, Map.Entry<IndexedWord, List<String>> entry2, Adw adw){
        this.entry1 = entry1;
        this.entry2 = entry2;
        this.adw = adw;
    }

    public void run() {
        System.out.format("%10s : ", this.docPair);
        for (Map.Entry<IndexedWord, List<String>> entry_doc1 : this.oracle.getWNsynsets(docPair.first()).entrySet()) {
            for (Map.Entry<IndexedWord, List<String>> entry_doc2 : this.oracle.getWNsynsets(docPair.second()).entrySet()) {
                System.out.format("%s_%d,%s_%d:%f ",
                        entry_doc1.getKey().word(),
                        entry_doc1.getKey().index(),
                        entry_doc2.getKey().word(),
                        entry_doc2.getKey().index(),
                        this.adw.getBestMatchScore(entry_doc1.getValue(), entry_doc2.getValue()));
            }
        }
        System.out.println();
    }

//    public void run() {
//        System.out.format("%s_%d,%s_%d:%f ",
//                this.entry1.getKey().word(),
//                this.entry1.getKey().index(),
//                this.entry2.getKey().word(),
//                this.entry2.getKey().index(),
//                this.adw.getBestMatchScore(this.entry1.getValue(), this.entry2.getValue()));
//    }
}
