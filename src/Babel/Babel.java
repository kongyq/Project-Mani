package Babel;

import core.CoreMani;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyToken;
import it.uniroma1.lcl.babelfy.commons.PosTag;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by anonymous on 2/21/18.
 */
public class Babel {

    //Babelfy
    private BabelfyParameters bp;
    private Babelfy bfy;

    //BabelNet
    private BabelNet bn;

    //
    private HashMap<IndexedWord, List<String>> synsetIDList;

    public Babel(){
        this.bp = new BabelfyParameters();
        this.bp.setAnnotationResource(BabelfyParameters.SemanticAnnotationResource.WN);
        this.bp.setMCS(BabelfyParameters.MCS.ON_WITH_STOPWORDS);

        this.bfy = new Babelfy(this.bp);

        this.bn = BabelNet.getInstance();

    }

    public void parseDoc(LinkedList<SemanticGraph> doc) throws InvalidBabelSynsetIDException, IOException {
        this.synsetIDList = new HashMap<>();
        for (SemanticGraph sentence: doc){
            this.addSynsetID(sentence);
//            break;  //!!!!!!!!!!!!!!!!!!!!!!
        }
    }

    public void addSynsetID(SemanticGraph sentence) throws InvalidBabelSynsetIDException, IOException {

        List<BabelfyToken> tokens = new ArrayList<>();
//        sentence.vertexListSorted().stream().forEach(word-> System.out.print(word.word() + ' '));

        List<IndexedWord> idxWordList = sentence.vertexListSorted();

        for (IndexedWord word: idxWordList){
            if (word.tag() == "."){
                tokens.add(BabelfyToken.EOS);
            }else {
                tokens.add(new BabelfyToken(word.word(), word.lemma(), tagToPosTag(word.tag()), Language.EN));
            }
        }

        List<SemanticAnnotation> annotations = this.bfy.babelfy(tokens, Language.EN);

        for (SemanticAnnotation annotation: annotations){
//            System.out.println(annotation.getTokenOffsetFragment().getStart());
//            System.out.println(tokens.get(annotation.getTokenOffsetFragment().getStart()).getWord());
            List<String> wordnetIDs = bn.getSynset(new BabelSynsetID(annotation.getBabelSynsetID()))
                    .getWordNetOffsets()
                    .stream()
                    .map(obj -> obj.getSimpleOffset())
                    .collect(Collectors.toList());
//            wordnetIDs.forEach(id -> System.out.println(id));
//            System.out.println(sentence.getNodeByIndex(annotation.getTokenOffsetFragment().getEnd()).word());
            this.synsetIDList.put(idxWordList.get(annotation.getTokenOffsetFragment().getStart()),
                    new ArrayList<>(wordnetIDs));
        }

    }

    public HashMap<IndexedWord, List<String>> getSynsetIDList(){
        return this.synsetIDList;
    }

    public PosTag tagToPosTag (String tag){
        String[] noun = {"NN", "NNS", "NNP", "NNPS"};
        String[] verb = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
        String[] adv = {"RB", "RBR", "RBS"};
        String[] adj = {"JJ", "JJR", "JJS"};

        if (Arrays.asList(noun).contains(tag)){
            return PosTag.NOUN;
        }else if(Arrays.asList(verb).contains(tag)){
            return PosTag.VERB;
        }else if(Arrays.asList(adv).contains(tag)){
            return PosTag.ADVERB;
        }else if(Arrays.asList(adj).contains(tag)){
            return PosTag.ADJECTIVE;
        }else{
            return PosTag.OTHER;
        }
    }

}
