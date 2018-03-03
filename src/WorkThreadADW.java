import edu.stanford.nlp.util.Pair;
import wordnet.Adw;

/**
 * Created by mike on 3/1/18.
 */
public class WorkThreadADW implements Runnable{

    private String text1;
    private String text2;
    private Adw adw;
    private Pair<Integer, Integer> docPair;

    public WorkThreadADW(String text1, String text2, Pair<Integer,Integer> docPair){
        this.text1 = text1;
        this.text2 = text2;
        this.adw = new Adw();
        this.docPair = docPair;
    }

    public void run(){
        System.out.format("%10s : %f%n", docPair,
                this.adw.getTextSimilarity(this.text1,this.text2));
    }
}
