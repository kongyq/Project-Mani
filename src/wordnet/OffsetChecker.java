package wordnet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mike on 2/21/18.
 */
public class OffsetChecker {
    public void test() throws IOException {
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);

        IDictionary dict = new Dictionary(new File("resources/WordNet-3.0/dict"));
        dict.open();

        IIndexWord idxWord = dict.getIndexWord("senator", POS.NOUN);
        IWordID wordID = idxWord.getWordIDs().get(0);
        IWord word = dict.getWord(wordID);
        System.out.println("Id = " + wordID);
        System.out.println("Lemma = " + word.getLemma());
        System.out.println("Gloss = " + word.getSynset().getGloss());
    }

    public static void main (String[] args) throws IOException {
        OffsetChecker offsetChecker  = new OffsetChecker();
        offsetChecker.test();
    }
}
