package wordnet;

import edu.sussex.nlp.jws.JWS;
import edu.mit.jwi.item.POS;
import edu.sussex.nlp.jws.WuAndPalmer;

import javax.sound.midi.Soundbank;

/**
 * Created by mike on 12/5/17.
 */
public class WordSim {

    private JWS jws;

    public WordSim(){
        this.jws = new JWS("./lib", "3.0");
    }

    public double wupSimilarity(String word1, String word2, String pos){
        WuAndPalmer wup = jws.getWuAndPalmer();
        return wup.max(word1, word2, pos);
    }

    public static void main (String[] args){
        WordSim wordSim = new WordSim();
        System.out.println(wordSim.wupSimilarity("dog", "dog", "n"));
    }
}
