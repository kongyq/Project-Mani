package wordnet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by anonymous on 2/21/18.
 */
public class OffsetChecker {
    public void test() throws IOException {
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);

        IDictionary dict = new Dictionary(new File("resources/WordNet-3.0/dict"));
        dict.open();

        ISynsetID iSynsetID = new SynsetID(14410605, POS.NOUN);
        ISynset iSynset = dict.getSynset(iSynsetID);
        System.out.println("Gloss = " +iSynset.getGloss());
        List<IWord> iWordList = iSynset.getWords();
        for(IWord iWord: iWordList){
            System.out.println("Lemma = " + iWord.getLemma());
        }

//        IIndexWord idxWord = dict.getIndexWord("senator", POS.NOUN);
//        IWordID wordID = idxWord.getWordIDs().get(0);
//        IWord word = dict.getWord(wordID);
//        System.out.println("Id = " + wordID);
//        System.out.println("Lemma = " + word.getLemma());
//        System.out.println("Gloss = " + word.getSynset().getGloss());
    }

    public static void main (String[] args) throws IOException {
        OffsetChecker offsetChecker  = new OffsetChecker();
        offsetChecker.test();
    }
}
