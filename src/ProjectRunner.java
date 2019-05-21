import com.google.common.base.Stopwatch;
import corenlp.CoreNlp;
import edu.stanford.nlp.util.Pair;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import reader.DocReader;
import reader.EvalReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by anonymous on 11/20/17.
 */
public class ProjectRunner {

    private static final File DATASET = new File("./corpus/documents.txt");
    private static final File EVAL = new File("./corpus/sim.csv");

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, InvalidBabelSynsetIDException {
        Stopwatch timer = Stopwatch.createStarted();
        DocReader docReader = new DocReader();
        docReader.readShortArticle(DATASET);

        HashMap<Integer, String> docs = docReader.getShortArticles();
        Set<Pair<Integer, Integer>> docPairs = getDocIndexPair(docs.keySet());
//        System.out.println(docPairs);

        System.out.println("Start!");

        //final results output file
        PrintStream out = new PrintStream(new FileOutputStream("./results/ManiOutput_Babel_ADW_NVPOS_NOSW_FULL.txt"));
        System.setOut(out);

        //create executor for multithreads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        EvalReader evalReader = new EvalReader();
        evalReader.read(EVAL);
        HashMap<Pair<Integer, Integer>, Double> evalMatrix = evalReader.getSim();

        //HashMap store maniScore as Double array with different thresholds.
        HashMap<Pair<Integer, Integer>, Double[]> maniMatrix = new HashMap<>();

        List<Future<Pair<Pair<Integer, Integer>, Double[]>>> threadList = new ArrayList<>();

        //TODO for save tokens!!!!!!
        GenerateOracle oracle = new GenerateOracle(docs);

        for (Pair<Integer, Integer> docPair: docPairs) {
            //System.out.println(docPair);

            CoreNlp coreNlp1 = new CoreNlp();
            CoreNlp coreNlp2 = new CoreNlp();
            coreNlp1.read(docs.get(docPair.first()));
            coreNlp2.read(docs.get(docPair.second()));

//            Future<Pair<Pair<Integer, Integer>, Double[]>> futureCall = executor.submit(new CoreThread(docPair, coreNlp1, coreNlp2, new Double[]{1D, 0.95D, 0.9D, 0.85D, 0.8D, 0.75D}));
            Future<Pair<Pair<Integer, Integer>, Double[]>> futureCall = executor.submit(
                    new CoreThread(
                            docPair,
                            oracle.getWNsynsets(docPair.first()),
                            oracle.getWNsynsets(docPair.second()),
                            coreNlp1, coreNlp2,
                            new Double[]{1D, 0.95D, 0.9D, 0.85D, 0.8D, 0.75D, 0.7D, 0.65D, 0.6D, 0.55D, 0.5D, 0.45D, 0.4D, 0.35D, 0.3D, 0.25D, 0.2D, 0.15D, 0.1D, 0.05D, 0D}));

//            Future<Pair<Pair<Integer, Integer>, Double[]>> futureCall = executor.submit(new CoreThread(docPair, coreNlp1, coreNlp2, new Double[]{1D}));

            threadList.add(futureCall);
        }

        for (Future<Pair<Pair<Integer, Integer>, Double[]>> future: threadList){

            Pair<Integer,Integer> docPair = future.get().first();

            Double[] maniScores = future.get().second();

            maniMatrix.put(docPair, maniScores);
            System.out.format("%10s : ", docPair);
            for (Double score: maniScores){
                System.out.format("%11.4f ", score);
            }
            System.out.println();

        }

        executor.shutdown();
        System.out.println("Total running time: " + timer.stop());

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

    public static List<Double> computeCorrelation(HashMap<Pair<Integer, Integer>, Double[]> maniScores, HashMap<Pair<Integer, Integer>, Double> eval){

        List<Double> correlation = new LinkedList<>();

//        Double[] correlation = {0.0,0.0,0.0};
        for (int i = 0; i < maniScores.values().iterator().next().length; i++){
            Double sumX = 0.0;
            Double sumY = 0.0;
            Double sumX2 = 0.0;
            Double sumY2 = 0.0;
            Double sumXY = 0.0;
            Integer n = maniScores.size();
            for (Map.Entry<Pair<Integer, Integer>, Double[]> entry: maniScores.entrySet()){
                sumX += entry.getValue()[i];
                sumX2 += Math.pow(entry.getValue()[i], 2);
                sumY += eval.get(entry.getKey());
                sumY2 += Math.pow(eval.get(entry.getKey()), 2);
                sumXY += entry.getValue()[i] * eval.get(entry.getKey());
            }
            correlation.add((n*sumXY - sumX*sumY)/(Math.sqrt(n*sumX2 - Math.pow(sumX,2)) * Math.sqrt(n*sumY2 - Math.pow(sumY,2))));
        }
        return correlation;
    }

    public static List<Pair<Integer, Integer>> getBin(HashMap<Pair<Integer, Integer>, Double> eval, Double min, Double max, Set<Pair<Integer,Integer>> maniIndex){
        List<Pair<Integer, Integer>> bin = new ArrayList<>();
        for (Pair<Integer, Integer> index: maniIndex){
            if (eval.get(index) >= min && eval.get(index) <= max){
                bin.add(index);
            }
        }
        return bin;
    }

    public static Pair<Double, Double> computeDistribution(HashMap<Pair<Integer, Integer>, Double[]> maniMatrix, List<Pair<Integer, Integer>> bin, Integer i){
        Integer size = bin.size();

        List<Double> values = bin.stream().map(pair -> maniMatrix.get(pair)[i]).collect(Collectors.toList());

        System.out.format("min: %.6f max: %.6f%n",values.stream().mapToDouble(value -> value).min().getAsDouble(),
                values.stream().mapToDouble(value -> value).max().getAsDouble());

        System.out.println(values);

        Double mean = values.stream().mapToDouble(value -> value).average().getAsDouble();

        Double dev = values.stream().mapToDouble(value -> Math.pow((value-mean), 2)).average().getAsDouble();

        return new Pair<>(mean, dev);
    }
}
