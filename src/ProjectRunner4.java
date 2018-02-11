import core.Core;
import corenlp.CoreNlp;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.xml.sax.SAXException;
import reader.DocReader;
import tfidf.CorpusParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by mike on 11/7/17.
 */
public class ProjectRunner4 {
    private static final File folder = new File("/home/mike/Documents/corpus/TmpCorpus/");
    private static final File TFIDF = new File("/home/mike/Documents/corpus/TxtAonlyCorpus");
    private static final File eval = new File("/home/mike/Documents/corpus/all.scores.sorted2");

    public ProjectRunner4(){

    }

    public static void main (String[] args) throws IOException, SAXException, BoilerpipeProcessingException {

        CorpusParser parser = new CorpusParser();
        parser.parseFiles(TFIDF);
        parser.tfIdfCalculator();

        List<String> lines = Files.readAllLines(eval.toPath(), StandardCharsets.UTF_8);

        Integer count = lines.size();
        Integer[] match = {0,0,0,0};

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/ManiOutput7.txt"));
        System.setOut(out);

        for (String line: lines) {
            String comparer = line.split(" ")[0];
            String compareeleft = line.split(" ")[1];
            String compareeright = line.split(" ")[2];
            Integer answer = Integer.valueOf(line.split(" ")[4]);

            Integer[] TMRP = {0,0,0,0};

            File comparerFullname = new File(folder.toString() + '/' +comparer);
            File compareeleftFullname = new File(folder.toString() + '/' + compareeleft);
            File compareerightFullname = new File(folder.toString() + '/' + compareeright);

            Double[] resultleft = getSimilarity(comparerFullname, compareeleftFullname, parser);
            Double[] resultright = getSimilarity(comparerFullname, compareerightFullname, parser);

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

            System.out.format("%10s %10s %10s : TFIDF:(%.6f %.6f)\tManiScore:(%12.6f %12.6f)\tRatio:(%.6f %.6f)\tProduct:(%12.6f %12.6f)\tTMRP: %d %d %d %d A: %d\n",
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
                    TMRP[0],
                    TMRP[1],
                    TMRP[2],
                    TMRP[3],
                    answer);
        }
        System.out.format("Accuracy: TFIDF: %.4f ManiScore: %.4f Ratio: %.4f Product: %.4f\n",
                (double)match[0] / count,
                (double)match[1]/count,
                (double)match[2]/count,
                (double)match[3]/count);
    }

    public static File getTxtName(File htmlFile){
        return new File(htmlFile.toString().replaceFirst("TmpCorpus", "TxtAonlyCorpus").replaceFirst("html", "txt"));
    }

    public static Integer whichBetter(Double[] left, Double[] right, Integer index){
        if (left[index] >= right[index]) {
            return 0;
        }else{
            return 1;
        }
    }

    public static Double[] getSimilarity(File doc1, File doc2, CorpusParser parser) throws IOException, BoilerpipeProcessingException, SAXException {
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
        return new Double[] {parser.getCosineSimilarity(getTxtName(doc1), getTxtName(doc2)),
                core.getSimilarity(),
                core.getJaccardSimilarity(),
                core.getJaccardSimilarity() * core.getSimilarity()};
    }

}
