import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import briefcase.Briefcase;
import core.Core;
import corenlp.CoreNlp;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import edu.stanford.nlp.util.Pair;
import org.xml.sax.SAXException;
import reader.DocReader;
import tfidf.CorpusParser;

/**
 * Created by mike on 11/6/17.
 */
public class ProjectRunner3 {
    private static final File folder = new File("/home/mike/Documents/corpus/TmpCorpus/");
    private static final File TFIDF = new File("/home/mike/Documents/corpus/TxtCorpus");
    private static final File eval = new File("/home/mike/Documents/corpus/all.scores.sorted2");

    public static Integer whichBetter(Double[] left, Double[] right, Integer index){
        if (left[index] >= right[index]) {
            return 0;
        }else{
            return 1;
        }
    }

    public static void main(String[] args) throws IOException, SAXException, BoilerpipeProcessingException, ExecutionException, InterruptedException {
//        HashMap<Pair<String,String>, HashMap<String, Double>> quickTable = new HashMap<>();

//        Briefcase briefcase = new Briefcase(folder, "html");

        //create a instance for TFIDF score
        CorpusParser corpusParser = new CorpusParser();
        corpusParser.parseFiles(TFIDF);
        corpusParser.tfIdfCalculator();

        //create executor for multithreads
        ExecutorService executor = Executors.newFixedThreadPool(4);


        List<String> lines = Files.readAllLines(eval.toPath(), StandardCharsets.UTF_8);

        Integer count = lines.size();
        Integer[] match = {0,0,0,0,0,0,0};

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/ManiOutput_MultiThreads_stemmed_TxM.txt"));
        System.setOut(out);

        for (String line: lines){
            String comparer = line.split(" ")[0];
            String compareeleft = line.split(" ")[1];
            String compareeright = line.split(" ")[2];
            Integer answer = Integer.valueOf(line.split(" ")[4]);

//            Double[] resultleft = {0.0,0.0,0.0,0.0};
//            Double[] resultright = {0.0,0.0,0.0,0.0};
            Integer[] TMRP = {0,0,0,0,0};

            File comparerFullname = new File(folder.toString() + '/' +comparer);
            File compareeleftFullname = new File(folder.toString() + '/' + compareeleft);
            File compareerightFullname = new File(folder.toString() + '/' + compareeright);

            Future<Double[]> futureCall1 = executor.submit(new WorkerThread2(comparerFullname, compareeleftFullname, corpusParser));
            Double[] resultleft = futureCall1.get();

            Future<Double[]> futureCall2 = executor.submit(new WorkerThread2(comparerFullname, compareerightFullname, corpusParser));
            Double[] resultright = futureCall2.get();

//            Runnable worker1 = new WorkerThread2(comparerFullname, compareeleftFullname, corpusParser, resultleft);
//            executor.execute(worker1);
//            Runnable worker2 = new WorkerThread2(comparerFullname, compareerightFullname, corpusParser, resultright);
//            executor.execute(worker2);

            if ((TMRP[0] = whichBetter(resultleft, resultright, 0)) == answer){
                match[0] ++;
            }
            if ((TMRP[1] = whichBetter(resultleft, resultright, 1)) == answer){
                match[1] ++;
            }
            if ((TMRP[2] = whichBetter(resultleft, resultright, 2)) == answer){
                match[2] ++;
            }
            if ((TMRP[3] = whichBetter(resultleft, resultright, 3)) == answer){
                match[3] ++;
            }
            if ((TMRP[0] == answer) || (TMRP[1] == answer)){
                match[4] ++;
            }
            if ((TMRP[0] == answer) || (TMRP[3] == answer)){
                match[5] ++;
            }
            if ((TMRP[4] = whichBetter(resultleft, resultright, 4)) == answer){
                match[6] ++;
            }

            System.out.format("%10s %10s %10s : TFIDF:(%.6f %.6f)\tManiScore:(%12.6f %12.6f)\tRatio:(%.6f %.6f)\tProduct:(%11.6f %11.6f)\tPP:(%11.6f %11.6f)\tTMRP: %d %d %d %d %d A: %d\n",
                    comparer,
                    compareeleft,
                    compareeright,
                    resultleft[0],
                    resultright[0],
                    resultleft[1],
                    resultright[1],
                    resultleft[2],
                    resultright[2],
                    resultleft[3],
                    resultright[3],
                    resultleft[4],
                    resultright[4],
                    TMRP[0],
                    TMRP[1],
                    TMRP[2],
                    TMRP[3],
                    TMRP[4],
                    answer);
        }
        executor.shutdown();
        System.out.format("Accuracy: TFIDF: %.4f ManiScore: %.4f Ratio: %.4f Product: %.4f T+M: %.4f T+P: %.4f PP: %.4f\n",
                (double)match[0] / count,
                (double)match[1]/count,
                (double)match[2]/count,
                (double)match[3]/count,
                (double)match[4]/count,
                (double)match[5]/count,
                (double)match[6]/count);
    }

    public static Double[] getScores(File root, File left, File right) throws BoilerpipeProcessingException, IOException, SAXException {
        Double leftScore = getSimilarity(root, left)[0];
        Double rightScore = getSimilarity(root, right)[0];
        Double output;
        if (leftScore >= rightScore){
            output = 0.0;
        }else{
            output = 1.0;
        }
        return new Double[] {leftScore, rightScore, output};
    }

    public static Double[] getSimilarity(File doc1, File doc2) throws IOException, BoilerpipeProcessingException, SAXException {
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
        return new Double[] {core.getSimilarity(), core.getJaccardSimilarity()};
    }
}
