package corenlp;

/**
 * Created by mike on 10/13/17.
 */
import com.google.common.base.CharMatcher;
import core.Core;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import mani.Mani;
import org.apache.commons.io.IOUtils;
import reader.DocReader;
import stemmer.Stemmer;
import stopword.StopwordAnnotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static core.Core.getDiameter;

public class CoreNlp {

    private static final File STOPWORDLIST = new File("./src/stopword/stopwords2.txt");

    private StanfordCoreNLP pipeline;
    private Annotation document;
    private Mani mani;
    private ArrayList<String> stemmedTerms;

    private LinkedList<SemanticGraph> sentenceList;

    public void read(String rawText){
        // read some text in the text variable
        // text = "...";

        // create an empty Annotation just with the given text
        this.document = new Annotation(rawText);
//        System.out.println(this.document.size());
        // run all Annotators on this text
        pipeline.annotate(this.document);
//        this.createMani();
        this.createSentenceList();
    }


    public static String loadStopWordList(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String stopwords = IOUtils.toString(inputStream, "UTF-8");
        return stopwords.replaceAll("\n", ",");
    }

    public CoreNlp(String flag) throws IOException {
        Properties props = new Properties();
        //props.setProperty("annotators", "tokenize, ssplit, pos");
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
        props.setProperty("customAnnotatorClass.stopword", "stopword.StopwordAnnotator");
        props.setProperty(StopwordAnnotator.STOPWORDS_LIST, loadStopWordList(STOPWORDLIST));
        this.pipeline = new StanfordCoreNLP(props);
        this.stemmedTerms = new ArrayList<>();
    }

    public CoreNlp() throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        //props.setProperty("annotators", "tokenize, ssplit, pos");
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword, depparse");
        props.setProperty("customAnnotatorClass.stopword", "stopword.StopwordAnnotator");
        props.setProperty(StopwordAnnotator.STOPWORDS_LIST, loadStopWordList(STOPWORDLIST));
        this.pipeline = new StanfordCoreNLP(props);
        this.stemmedTerms = new ArrayList<>();

    }

    public StanfordCoreNLP getPipeline(){
        return this.pipeline;
    }

    public List<CoreMap> getSentences(){
        return this.document.get(SentencesAnnotation.class);
    }

    public void createStemmedTerms() {
        Stemmer stemmer = new Stemmer();
        List<CoreMap> sentences = this.getSentences();
        for (CoreMap sentence : sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);
                String word = stemmer.stem(token.get(TextAnnotation.class));
                if (!stopword.first() && !stopword.second() && !word.matches("\\p{Punct}+") && !word.matches("\\p{Digit}+")) {
                    this.stemmedTerms.add(word.toLowerCase());
                }
            }
        }
    }

    public void createSentenceList(){
        List<CoreMap> sentences = this.getSentences();
        this.sentenceList = new LinkedList<>();
        for (CoreMap sentence: sentences){
            SemanticGraph depparse = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//            System.out.println(depparse.size());
            this.sentenceList.add(depparse);
        }
    }

    public void createMani(){
        List<CoreMap> sentences = this.getSentences();
        this.mani = new Mani();
        Stemmer stemmer = new Stemmer();
//        Mani mani = new Mani();

        for (CoreMap sentence: sentences){

            SemanticGraph dependencies  = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//            System.out.println(dependencies.getFirstRoot().docID());

//            SemanticGraph dependencies  = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
//            int count = 0;
//            IndexedWord word1 = null;
//            IndexedWord word2 = null;

            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//                System.out.format("%d, %d%n",token.sentIndex(), token.index());
//                System.out.println(token.index());
                Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);
                String word = stemmer.stem(token.get(TextAnnotation.class));
//                String word = stemmer.stem(token.get(LemmaAnnotation.class));

//                String pos = token.get(PartOfSpeechAnnotation.class);
//                System.out.println(pos);
//                Integer index = token.get(IndexAnnotation.class);
//                String lemma = token.get(LemmaAnnotation.class);
                //String ner = token.get(NERIDAnnotation.class);
//                String word = token.get(LemmaAnnotation.class);
//                System.out.println(word);
                //test only
//                if (count == 0){
//                    word1 = new IndexedWord(token);
//                }else if(count == 18){
//                    word2 = new IndexedWord(token);
//                }
                //

//                System.out.println(word);
//                System.out.println(stopword.first());
//                System.out.println(pos);
//                System.out.println(index);
//                System.out.println(lemma);
//                System.out.println(ner);
                if (!stopword.first() &&
                        !stopword.second() &&
                        !word.matches("\\p{Punct}+") /*&&
                        !word.matches("\\p{Digit}+")*/ &&
                        !word.matches("\\p{Blank}+") &&
                        !word.startsWith("http")) {
                    this.mani.put(word.toLowerCase(), new IndexedWord(token), dependencies);
//                    this.stemmedTerms.add(word.toLowerCase());
                }

//                if (!stopword.first() && !stopword.second() && !lemma.matches("\\p{Punct}")) {
//                    this.mani.put(lemma.toLowerCase(), new IndexedWord(token), dependencies);
//                }
//                count ++;
            }
//            SemanticGraph dependencies  = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
//            dependencies.prettyPrint();
//            System.out.println(dependencies);
//            System.out.println(getDiameter(dependencies)[1]);
//            System.out.println(getDiameter(dependencies)[0]);
//            System.out.println(word1);
//            System.out.println(word2);
//            System.out.println(dependencies.getShortestUndirectedPathNodes(word1,word2));
//            break;

        }
//        System.out.println(mani.getMani());
    }

    public Mani getMani(){
        return this.mani;
    }

    public LinkedList<SemanticGraph> getSentenceList(){return this.sentenceList;}

    public ArrayList<String> getStemmedTerms(){
        return this.stemmedTerms;
    }

    public static void main(String[] arg) throws IOException {
        DocReader dr1 = new DocReader();
        DocReader dr2 = new DocReader();
        dr1.read(new File("/home/mike/Documents/corpus/orig/nklm/3.txt"));
        dr2.read(new File("/home/mike/Documents/corpus/orig/nklm/2.txt"));

        CoreNlp coreNlp1 = new CoreNlp();
        CoreNlp coreNlp2 = new CoreNlp();

        coreNlp1.read(dr1.getText());
        coreNlp1.createMani();
//        System.out.println(coreNlp1.getMani());

        coreNlp2.read(dr2.getText());
        coreNlp2.createMani();

        Core core = new Core(coreNlp1.getMani(), coreNlp2.getMani());
        System.out.println(core.getSimilarity());


    }

    public static void main2(String[] args) throws IOException {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(loadStopWordList(new File("./src/stopword/stopwords1.txt")));
    }

}