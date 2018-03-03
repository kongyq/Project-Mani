import Babel.Babel;
import com.google.common.base.Stopwatch;
import corenlp.CoreNlp;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import reader.DocReader;
import wordnet.Adw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 2/21/18.
 */
public class GenerateOracle {

    //<DocID, <IndexedWord, WordnetID>>
    private HashMap<Integer, HashMap<IndexedWord, List<String>>> Oracle;
    private CoreNlp coreNlp;
    private Babel babel;

    public GenerateOracle(HashMap<Integer, String> docs) throws IOException, InvalidBabelSynsetIDException {
        this.Oracle = new HashMap<>();

        this.coreNlp = new CoreNlp();

        this.babel = new Babel();

        for (Map.Entry<Integer, String> entry: docs.entrySet()){
            Integer docID = entry.getKey();
            String text = entry.getValue();

            this.coreNlp.read(text);
            this.coreNlp.createSentenceList();
//            this.coreNlp.createMani();
            this.babel.parseDoc(this.coreNlp.getSentenceList());

            Oracle.put(docID, new HashMap<>(this.babel.getSynsetIDList()));

//            break;
        }
    }

    public HashMap<IndexedWord, List<String>> getWNsynsets(Integer docID){
        return this.Oracle.get(docID);
    }

    //*******************************************FOR Python******************************************************

    public static Set<Pair<Integer, Integer>> getDocIndexPair(Set<Integer> docIndex){
        Set<Pair<Integer, Integer>> docIndexPair = new HashSet<>();
        for (int i = 1; i <= docIndex.size(); i++){
            for (int j = i+1; j <= docIndex.size(); j++){
                docIndexPair.add(new Pair<>(i, j));
            }
        }
        return docIndexPair;
    }

    public static void main(String[] args) throws IOException, InvalidBabelSynsetIDException {
        File DATASET = new File("/home/mike/Documents/corpus/documents.txt");
//        Stopwatch timer = Stopwatch.createStarted();

        DocReader docReader = new DocReader();
        docReader.readShortArticle(DATASET);

        HashMap<Integer, String> docs = docReader.getShortArticles();

        GenerateOracle oracle = new GenerateOracle(docs);

        Adw adw = new Adw();

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/BabelNet_dictory_for_python_V2.txt"));
        System.setOut(out);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Set<Pair<Integer, Integer>> docPairs = getDocIndexPair(docs.keySet());

        for (Pair<Integer, Integer> docPair: docPairs){

//            Runnable worker = new WorkerThreadGO(docPair, oracle, adw);
//            executor.execute(worker);

            System.out.format("%10s : ", docPair);
            for (Map.Entry<IndexedWord, List<String>> entry_doc1: oracle.getWNsynsets(docPair.first()).entrySet()){
                for (Map.Entry<IndexedWord, List<String>> entry_doc2: oracle.getWNsynsets(docPair.second()).entrySet()){

//                    Runnable worker = new WorkerThreadGO(entry_doc1, entry_doc2, adw);
//                    executor.execute(worker);
//
                    System.out.format("%s#%d#%d,%s#%d#%d:%f ",
                            entry_doc1.getKey().word(),
                            entry_doc1.getKey().index(),
                            entry_doc1.getKey().sentIndex(),
                            entry_doc2.getKey().word(),
                            entry_doc2.getKey().index(),
                            entry_doc2.getKey().sentIndex(),
                            adw.getBestMatchScore(entry_doc1.getValue(), entry_doc2.getValue()));
//

                }
            }
            System.out.println();
//            break;
        }

        executor.shutdown();
//        System.out.println("Total running time: " + timer.stop());

    }

}
