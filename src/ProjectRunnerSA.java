import com.google.protobuf.MapEntry;
import core.Core;
import core.CoreMani;
import corenlp.CoreNlp;
import edu.mit.jwi.item.IndexWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.Pair;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import reader.DocReader;
import reader.EvalReader;
import edu.cmu.lti.jawjaw.pobj.POS;
import wordnet.Adw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by mike on 11/20/17.
 */
public class ProjectRunnerSA {

    private static final File DATASET = new File("/home/mike/Documents/corpus/documents.txt");
    private static final File EVAL = new File("/home/mike/Documents/corpus/sim.csv");

    public static void main(String[] args) throws IOException, InvalidBabelSynsetIDException, InterruptedException {
        DocReader docReader = new DocReader();
        docReader.readShortArticle(DATASET);

        HashMap<Integer, String> docs = docReader.getShortArticles();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/ADW_TEXT.txt"));
        System.setOut(out);

        Set<Pair<Integer, Integer>> docPairs = getDocIndexPair(docs.keySet());

//        Adw adw = new Adw();
//        TimeUnit.SECONDS.sleep(3);
//        Thread.sleep(10000);

        for (Pair<Integer, Integer> docPair: docPairs){
//            Thread.sleep(5000);
            Runnable worker = new WorkThreadADW(
                    docs.get(docPair.first()),
                    docs.get(docPair.second()),
                    docPair);
            executor.execute(worker);

        }
        executor.shutdown();
//        GenerateOracle oracle = new GenerateOracle(docs);
//        for (Map.Entry<IndexedWord, List<String>> entry : oracle.getWNsynsets(1).entrySet()){
//            System.out.format("%s, %s" , entry.getKey().word(), entry.getValue().get(0));
//        }

//        CoreNlp coreNlp1 = new CoreNlp();
//        CoreNlp coreNlp2 = new CoreNlp();
//        System.out.println(docs.get(1));
//        System.out.println(docs.get(14));
//        coreNlp1.read(docs.get(1));
//        coreNlp2.read(docs.get(14));

//        coreNlp1.createMani();

//        System.out.println(coreNlp1.getSentenceList());



//        CoreMani coreMani = new CoreMani(coreNlp1.getSentenceList(), coreNlp2.getSentenceList(),
//                oracle.getWNsynsets(1), oracle.getWNsynsets(14));
//
//        coreMani.run(new POS[] {POS.n, POS.v}, true);
//        System.out.println(coreMani.getManiScore_threshold(0.7D));

    }

    public static Set<Pair<Integer, Integer>> getDocIndexPair(Set<Integer> docIndex){
        Set<Pair<Integer, Integer>> docIndexPair = new HashSet<>();
        for (int i = 1; i <= docIndex.size(); i++){
            for (int j = i+1; j <= docIndex.size(); j++){
                docIndexPair.add(new Pair<>(i, j));
            }
        }
        return docIndexPair;
    }
//
//
//        Set<Pair<Integer, Integer>> docPairs = getDocIndexPair(docs.keySet());
////        System.out.println(docPairs);
//
//        EvalReader evalReader = new EvalReader();
//        evalReader.read(EVAL);
//        HashMap<Pair<Integer, Integer>, Double> evalMatrix = evalReader.getSim();
//
//        HashMap<Pair<Integer, Integer>, Double[]> maniMatrix = new HashMap<>();
//        for (Pair<Integer, Integer> docPair: docPairs){
////            System.out.println(docPair);
//            CoreNlp coreNlp1 = new CoreNlp();
//            CoreNlp coreNlp2 = new CoreNlp();
//            coreNlp1.read(docs.get(docPair.first()));
//            coreNlp2.read(docs.get(docPair.second()));
//
////            coreNlp1.createMani();
////            coreNlp2.createMani();
//
//            coreNlp1.createSentenceList();
//            coreNlp2.createSentenceList();
//
////            Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());
//            CoreMani coreMani = new CoreMani(coreNlp1.getSentenceList(), coreNlp2.getSentenceList());
//
////            maniMatrix.put(docPair, new Double[] {core.getSimilarity(), core.getJaccardSimilarity(), core.getJaccardSimilarity() * core.getSimilarity()});
////            maniMatrix.put();
//
////            System.out.format("%s, %.6f, %.6f%n",docPair, evalMatrix.get(docPair), core.getSimilarity());
//        }
//
//
//        Double[] correlation = computeCorrelation(maniMatrix, evalMatrix);
//        System.out.format("Correlation: %.6f, %.6f, %.6f.",
//                correlation[0], correlation[1], correlation[2]);
////        System.out.println(maniMatrix);
//
//    }
//
//    public static Set<Pair<Integer, Integer>> getDocIndexPair(Set<Integer> docIndex){
//        Set<Pair<Integer, Integer>> docIndexPair = new HashSet<>();
//        for (int i = 1; i <= docIndex.size(); i++){
//            for (int j = i+1; j <= docIndex.size(); j++){
//                docIndexPair.add(new Pair<>(i, j));
//            }
//        }
//        return docIndexPair;
//    }
//
//    public static Double[] computeCorrelation(HashMap<Pair<Integer, Integer>, Double[]> maniScores, HashMap<Pair<Integer, Integer>, Double> eval){
//        Double[] correlation = {0.0,0.0,0.0};
//        for (int i = 0; i < 3; i++){
//            Double sumX = 0.0;
//            Double sumY = 0.0;
//            Double sumX2 = 0.0;
//            Double sumY2 = 0.0;
//            Double sumXY = 0.0;
//            Integer n = maniScores.size();
//            for (Map.Entry<Pair<Integer, Integer>, Double[]> entry: maniScores.entrySet()){
//                sumX += entry.getValue()[i];
//                sumX2 += Math.pow(entry.getValue()[i], 2);
//                sumY += eval.get(entry.getKey());
//                sumY2 += Math.pow(eval.get(entry.getKey()), 2);
//                sumXY += entry.getValue()[i] * eval.get(entry.getKey());
//            }
//            correlation[i] = (n*sumXY - sumX*sumY)/(Math.sqrt(n*sumX2 - Math.pow(sumX,2)) * Math.sqrt(n*sumY2 - Math.pow(sumY,2)));
//        }
//        return correlation;
//    }

}
