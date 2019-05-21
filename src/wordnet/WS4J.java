package wordnet;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.File;
import java.util.List;

/**
 * Created by anonymous on 11/30/17.
 */
public class WS4J {

    private ILexicalDatabase db;
    private RelatednessCalculator rc;

    public WS4J(){
        this.db = new NictWordNet();
//        this.rc = new WuPalmer(this.db);
//        this.rc = new HirstStOnge(this.db);
//        this.rc = new JiangConrath(this.db);
        this.rc = new Lin(this.db);
        WS4JConfiguration.getInstance().setMFS(true);
//        WS4JConfiguration.getInstance().setStopList("stopwords2.txt");
    }

    public double wordSimilarity(String word1, String word2){
        List<POS[]> posPairs = this.rc.getPOSPairs();
        double maxScore = -1D;
        for (POS[] posPair: posPairs){
            double score = this.wordSimilarity(word1, posPair[0], word2, posPair[1]);
//            System.out.println(score);
            if (score > maxScore){
                maxScore = score;
            }
        }
        return maxScore;
    }

    public double compute(String w1, String w2){
        return rc.calcRelatednessOfWords(w1, w2);
    }

    public double wordSimilarity(String word1, String word2, POS pos){
        return wordSimilarity(word1, pos, word2, pos);
    }

    public double wordSimilarity(String word1, POS posWord1, String word2, POS posWord2) {
        double maxScore = 0D;
        double errorPOS = 0D;
        try {

            List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(word1, posWord1.name());
            List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(word2, posWord2.name());

            if (synsets1.isEmpty() || synsets2.isEmpty()){
                return errorPOS;
            }

            for (Concept synset1 : synsets1) {
                for (Concept synset2 : synsets2) {
//                    System.out.println(synset1);
                    Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
                    double score = relatedness.getScore();
                    if (score > 1D){
//                        System.out.format("ERROR: %f%n", score);
                        continue;
                    }
//                    System.out.println(score);
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
//            System.out.println("Similarity score of " + word1 + " & " + word2 + " : " + maxScore);
        } catch (Exception e) {
            System.out.println(e);
        }
        return maxScore;
    }



    public static void main(String[] args){
//        System.out.println(WS4J.class.getResource("/"));
//        System.out.println(compute("apple", "apple"));
        WS4J ws4J = new WS4J();

        System.out.println(ws4J.compute("wish", "will"));
//        System.out.println(ws4J.wordSimilarity("cat", "dog"));
//        System.out.println(ws4J.wordSimilarity("dog", POS.n, "cat", POS.n));
//        ws4J.wordSimilarity("dog", POS.n, "cat", POS.n);

        File test = new File("src/stopword/stopwords2.txt");
        System.out.println(test.getAbsolutePath());
        System.out.println(test.isFile());
    }


}
