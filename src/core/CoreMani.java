package core;

import com.google.common.base.Stopwatch;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.stanford.math.plex4.homology.barcodes.BarcodeCollection;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.Pair;
import homology.Plex;
import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.textual.similarity.TextualSimilarity;
import stemmer.Stemmer;
import stopword.StopwordAnnotator;
import wordnet.Adw;
//import wordnet.OovChecker;
import wordnet.WS4J;

import java.util.*;

/**
 * Created by anonymous on 12/7/17.
 */
public class CoreMani {

    //For POStag to POS format
    private static final String[] NOUN = {"NN", "NNS", "NNP", "NNPS"};
    private static final String[] VERB = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
    private static final String[] ADV = {"RB", "RBR", "RBS"};
    private static final String[] ADJ = {"JJ", "JJR", "JJS"};

    private LinkedList<SemanticGraph> doc1SentList;
    private LinkedList<SemanticGraph> doc2SentList;

    private HashMap<POS, ArrayList<IndexedWord>> wordList1;
    private HashMap<POS, ArrayList<IndexedWord>> wordList2;

    private Plex plex;  //JavaPlex instance
    private WS4J ws4J;  //WordNet word similarity matrix instance

    private Adw adw;    //ADW instance
//    private OovChecker oovChecker;  //ADW oov checker

    private Double maniScore;

    //for Jaccard similarity test only
    private Set<String> commonTerms = new HashSet<>();
    private Set<String> totalTerms = new HashSet<>();
    private Double jaccardSimilarity;

    //for Babel and ADW
    private HashMap<IndexedWord, List<String>> doc1Senses;
    private HashMap<IndexedWord, List<String>> doc2Senses;


    /***
     * Initial CoreMani class will two documents sentence list in SemanticGraph format.
     * @param doc1 Sentences list in doc1
     * @param doc2 Sentences list in doc2
     */
    public CoreMani(LinkedList<SemanticGraph> doc1, LinkedList<SemanticGraph> doc2,
                    HashMap<IndexedWord, List<String>> doc1Senses, HashMap<IndexedWord, List<String>> doc2Senses){
        this.doc1SentList = doc1;
        this.doc2SentList = doc2;
        this.wordList1 = this.initWordList();
        this.wordList2 = this.initWordList();
        this.plex = new Plex();
        this.ws4J = new WS4J();
        this.maniScore = 0D;

        this.doc1Senses = doc1Senses;
        this.doc2Senses = doc2Senses;

        //For TEST only
        //this.createTotalTerms();

        //For ADW TEST
        this.adw = new Adw();
//        this.oovChecker = new OovChecker();
    }

    /***
     * Initial HashMap by adding 4 POS (n, v, r, a) for words in the documents.
     * @param
     */
    private HashMap initWordList(){
        HashMap<POS, ArrayList<IndexedWord>> wordList = new HashMap<>();
        wordList.put(POS.n, new ArrayList<>());
        wordList.put(POS.v, new ArrayList<>());
        wordList.put(POS.r, new ArrayList<>());
        wordList.put(POS.a, new ArrayList<>());
        return wordList;
    }

    /***
     * Use WordNet similarity matrix to compute simplex filtration value.
     * @param word1 first IndexedWord
     * @param word2 second IndexedWord
     * @param pos Which kind of synset should be compare to compute similarity.
     * @return
     */
    public double getFiltration(IndexedWord word1, IndexedWord word2, POS pos){
        return 1-this.ws4J.wordSimilarity(word1.lemma(), word2.lemma(), pos);
    }

    //For ADW test
    public double getFiltration_ADW(IndexedWord word1, IndexedWord word2){

        Double score = 1D - this.adw.getBestMatchScore(this.doc1Senses.get(word1), this.doc2Senses.get(word2));
//        Double score = 1D - this.adw.getOffsetSimilarity(word1.lemma().toLowerCase(), word2.lemma().toLowerCase());
        return score;
    }

    /***
     * Convert coreNlp POS tag string to WordNet POS format
     * @param tag
     * @return
     */
    public static POS tagToPOS (String tag){
        String[] noun = {"NN", "NNS", "NNP", "NNPS"};
        String[] verb = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
        String[] adv = {"RB", "RBR", "RBS"};
        String[] adj = {"JJ", "JJR", "JJS"};

        if (Arrays.asList(noun).contains(tag)){
            return POS.n;
        }else if(Arrays.asList(verb).contains(tag)){
            return POS.v;
        }else if(Arrays.asList(adv).contains(tag)){
            return POS.r;
        }else if(Arrays.asList(adj).contains(tag)){
            return POS.a;
        }else{
            return null;
        }
    }

    /***
     * Use sentIndex and index methods in the IndexedWord structure to create JavaPlex vertex
     * @param indexedWord
     * @param docno this docno will be at the most front of the encoded vertex index.
     * @return
     */
    public static Integer indexedwordToVertex(IndexedWord indexedWord, Integer docno){
        if(indexedWord.sentIndex() > 9999 || indexedWord.index() > 999){
            System.out.println("Error, sentence index or word index is over bound!");
            return null;
        }else {
            String index = String.format("%d%04d%03d", docno, indexedWord.sentIndex(), indexedWord.index());
            return Integer.valueOf(index);
        }
    }

    /***
     * Is the IndexedWord stopword
     * @param indexedWord
     * @return true if the IndexedWord is stopword
     */
    public static boolean isStopword(IndexedWord indexedWord){
        Pair<Boolean, Boolean> stopword = indexedWord.backingLabel().get(StopwordAnnotator.class);
        if (stopword.first() || stopword.second()){
            return true;
        }else{
            return false;
        }
    }

    public static Set<Pair<IndexedWord, IndexedWord>> getAllAdjacentNodePairs(SemanticGraph sentence){
        return getAllAdjacentNodePairs(sentence, sentence.getFirstRoot());
    }

    public static Set<Pair<IndexedWord, IndexedWord>> getAllAdjacentNodePairs(SemanticGraph sentence, IndexedWord node){
        Set<Pair<IndexedWord, IndexedWord>> nodePairs = new HashSet<>();
        if (sentence.hasChildren(node)){
            for (IndexedWord child: sentence.getChildren(node)){
                nodePairs.add(new Pair<>(node, child));
                nodePairs.addAll(getAllAdjacentNodePairs(sentence, child));
            }
        }
        return nodePairs;
    }


    //For Jaccard Similarity test only
    public void createTotalTerms(){
        this.createTotalTerms(this.doc1SentList);
        this.commonTerms = new HashSet<>();
        this.createTotalTerms(this.doc2SentList);
    }

    public void createTotalTerms(List<SemanticGraph> sentList){
        Stemmer stemmer = new Stemmer();
        for (SemanticGraph sentences: sentList){
            for (IndexedWord word: sentences.vertexSet()){
                if (isStopword(word) || word.word().matches("\\p{Punct}+")){
                    continue;
                }

                String elem = stemmer.stem(word.word());

                if (this.totalTerms.contains(elem)){
                    this.commonTerms.add(elem);
                }
                this.totalTerms.add(stemmer.stem(word.word()));
            }
        }
    }


    /***
     * CORE ALG: Add words inside the sentence depparse tree amount one document, and convert those words into vertex and add them into simplex stream.
     * @param sentences
     * @param docno
     */
    private void addIntraDocConnection(LinkedList<SemanticGraph> sentences, Integer docno){
        for(SemanticGraph sentence: sentences) {

            //use addVertex method to add just one node if the sentence only contain one node(word).
            if (sentence.size() == 1) {
                this.plex.addVertex(indexedwordToVertex(sentence.getFirstRoot(), docno), 0);

            } else {    // use addElement method to add edges
                for (Pair<IndexedWord, IndexedWord> nodePair : getAllAdjacentNodePairs(sentence)) {
                    this.plex.addElement(indexedwordToVertex(nodePair.first(), docno),
                            indexedwordToVertex(nodePair.second(), docno),
                            0);
                }
            }
        }
    }

    /***
     * Create wordlist based on the POS of the indexedword by adding to the corresponding POS category.
     * @param sentences
     * @param wordList
     */
    private void createWordList(LinkedList<SemanticGraph> sentences, HashMap<POS, ArrayList<IndexedWord>> wordList, Boolean skipStopword){
        Stemmer stemmer = new Stemmer();
        for(SemanticGraph sentence: sentences) {

            for (IndexedWord word : sentence.vertexSet()) {
//                POS pos = tagToPOS(word.tag());
                if (tagToPOS(word.tag()) == null) {
                    continue;
                }
                //if word is stopword then skip
                if (isStopword(word) && skipStopword){
                    continue;
                }
                wordList.get(tagToPOS(word.tag())).add(word);
            }
        }
    }

    public void run(POS[] supportedPOS, Boolean skipStopword){

//        Stopwatch timer = Stopwatch.createStarted();
        this.addIntraDocConnection(this.doc1SentList, 1);
        this.addIntraDocConnection(this.doc2SentList, 2);
//        System.out.println("Adding Intra-doc Elements: " + timer.stop());
//        timer.reset();
//        timer.start();
        this.createWordList(this.doc1SentList, this.wordList1, skipStopword);
        this.createWordList(this.doc2SentList, this.wordList2, skipStopword);
//        System.out.println("Creating POS-IndexedWord HashMap: " + timer.stop());
//        timer.reset();
//        timer.start();
        for (POS pos: supportedPOS){
            this.addInterDocsConnection(pos);
        }
//        System.out.println("Adding Inter-docs Elements: " + timer.stop());
        this.plex.finalizeStream();
    }

    /***
     * CORE ALG: add document to document connection based on word to word similarity amount two documents by adding
     * same type of POS terms' similarity as the simplex stream filtration value.
     */
    private void addInterDocsConnection(POS pos){
        Stemmer stemmer = new Stemmer();
//        Stopwatch timer = Stopwatch.createUnstarted();  //timer
//        System.out.format("\tsize of %s IndexedWord: %d %d%n",
//                pos.name(),
//                this.wordList1.get(pos).size(),
//                this.wordList2.get(pos).size());

        for (IndexedWord indexedWord1: this.wordList1.get(pos)){
            for (IndexedWord indexedWord2: this.wordList2.get(pos)) {
                //if the stem of the two words are the same then add element with filtration value 0.0

//                System.out.println(indexedWord1.word() + " : " + indexedWord2.word());

                if (!this.doc1Senses.containsKey(indexedWord1) && !this.doc2Senses.containsKey(indexedWord2)) {
//                    System.out.println("OOV!");
                    if (stemmer.stem(indexedWord1.word()).equalsIgnoreCase(stemmer.stem(indexedWord2.word()))) {
//                        System.out.println("OOV EQUAL!");
                        this.plex.addElement(indexedwordToVertex(indexedWord1, 1),
                                indexedwordToVertex(indexedWord2, 2),
                                0D);
                        //For test only
//                    this.commonTerms.add(stemmer.stem(indexedWord1.word()));

//                        continue;
                    }
                    continue;
                }

                //if not, then use Wordnet to compute similarity between two words.
//                timer.start();
//                Double score = this.getFiltration(indexedWord1, indexedWord2, pos);
                if (this.doc1Senses.containsKey(indexedWord1) && this.doc2Senses.containsKey(indexedWord2)){
//                    System.out.println("IV!");
                    Double score = this.getFiltration_ADW(indexedWord1, indexedWord2);
//                    System.out.println(score);
//                timer.stop();
                    if (score >= 0D && score <= 1D) {
                        this.plex.addElement(indexedwordToVertex(indexedWord1, 1),
                                indexedwordToVertex(indexedWord2, 2),
                                score);

                        //For test only
                   /* if (score == 0D){
                        this.commonTerms.add(stemmer.stem(indexedWord1.word()));
                        this.commonTerms.add(stemmer.stem(indexedWord2.word()));
                    }*/

                    }else if (score > 1D){
                        System.out.format("ERROR: %f", score);
                    }
                }

            }
        }
//        System.out.println("\t\tWordNet Runtime: " + timer);
    }

    public void computeManiScore(){

    }
    public Double getJaccardSimilarity(){
        this.jaccardSimilarity = (double) this.commonTerms.size() / this.totalTerms.size();
        return this.jaccardSimilarity;
    }

    public BarcodeCollection getBarcode(){
        return this.plex.getBarcode();
    }

    public Double getManiScore_threshold(Double threshold){
        return this.plex.getBarcode(1).stream().mapToDouble(interval -> 1D - interval.getStart()).filter(interval ->  interval >= threshold).sum();
    }

    public Double getManiScore(Double P){
        LpNorm lpNorm = new LpNorm(this.plex.getBarcode(1), P);
        this.maniScore = lpNorm.getScore();
        return this.maniScore;
    }

    public List getIntervals(Integer dimension){return this.plex.getBarcode(dimension);}

    public static void main(String[] args){

    }
}
