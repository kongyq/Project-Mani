package mani;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.*;

/**
 * Created by anonymous on 9/14/17.
 */
public class Mani {

    /**
     * Main core data structure
     *     <Lemma, <IndexedWord, Sentence>>
     */
    private HashMap<String, LinkedHashMap<IndexedWord, SemanticGraph>> mani;

    public Mani(){
        this.mani = new HashMap<>();
    }

    public HashMap getMani(){
            return this.mani;
    }

    public void put(String lemma, IndexedWord indexedWord, SemanticGraph sentence){
        if (this.mani.containsKey(lemma)){
            this.mani.get(lemma).put(indexedWord, sentence);
        }
        else{
            this.mani.put(lemma, new LinkedHashMap<IndexedWord, SemanticGraph>());
            this.mani.get(lemma).put(indexedWord, sentence);
        }
    }

    public Set<String> getKeys(){
        return this.mani.keySet();
    }

    public LinkedHashMap<IndexedWord, SemanticGraph> getSentences(String lemma){
        return this.mani.get(lemma);
    }
}