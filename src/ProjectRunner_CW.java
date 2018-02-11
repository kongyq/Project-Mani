import core.Core;
import corenlp.CoreNlp;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import edu.stanford.nlp.util.Pair;
import org.xml.sax.SAXException;
import reader.DocReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by mike on 11/8/17.
 */
public class ProjectRunner_CW {

    public static final File eval = new File("/home/mike/Documents/corpus/all.scores.sorted2");
    private static final File folder = new File("/home/mike/Documents/corpus/TmpCorpus/");


    public static void main (String[] args) throws IOException, SAXException, BoilerpipeProcessingException {

        HashMap<Pair<String, String>, Set<String>> commonWordLists = new HashMap<>();
        Set<Pair<String, String>> filePairs = new HashSet<>();


        List<String> lines = Files.readAllLines(eval.toPath(), StandardCharsets.UTF_8);

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/ManiOutput_CW_2.txt"));
        System.setOut(out);


        for (String line: lines){
            String comparer = line.split(" ")[0];
            String compareeleft = line.split(" ")[1];
            String compareeright = line.split(" ")[2];
//            Integer answer = Integer.valueOf(line.split(" ")[4]);

            Pair<String, String> currentDocPairLeft = new Pair<>(comparer, compareeleft);
            Pair<String, String> currentDocPairRight = new Pair<>(comparer, compareeright);

            File comparerFullname = new File(folder.toString() + '/' +comparer);
            File compareeleftFullname = new File(folder.toString() + '/' + compareeleft);
            File compareerightFullname = new File(folder.toString() + '/' + compareeright);

            if (!filePairs.contains(currentDocPairLeft)){
                filePairs.add(currentDocPairLeft);
                System.out.format("%10s %10s: %s%n",
                        comparer,
                        compareeleft,
                        getCommonWords(comparerFullname, compareeleftFullname));

            }

            if (!filePairs.contains(currentDocPairRight)){
                filePairs.add(currentDocPairRight);
                System.out.format("%10s %10s: %s%n",
                        comparer,
                        compareeright,
                        getCommonWords(comparerFullname, compareerightFullname));

            }
        }
    }

    public static Set<String> getCommonWords(File doc1, File doc2) throws IOException, BoilerpipeProcessingException, SAXException {
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
        return core.getCommonWords();
    }

}
