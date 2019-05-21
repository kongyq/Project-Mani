package core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.Pair;
import mani.Mani;
import stopword.StopwordAnnotator;

import java.util.*;

/**
 * Created by anonymous on 10/26/17.
 */
public class Core {

    private Mani doc1;
    private Mani doc2;
    private Set<String> commonWords;
    private ArrayList<Pair<String,String>> cwCombination;

    private Double jaccardSimilarity;

    //for test purpose
    private HashMap<IndexedWord, Integer> word1SentMap;
    private HashMap<IndexedWord, Integer> word2SentMap;

    private HashMap<Pair<String, String>, ArrayList<Double>[]> weights;
    private ArrayList<Double> pairWeights;

    public Core(){
        this.doc1 = new Mani();
        this.doc2 = new Mani();
    }

    public Core(Mani doc1, Mani doc2){
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.computeCommonWords();
        this.computeCommonWordCombination();
        this.computeJaccardSimilarity();
    }

    /***
     * get common words between two documents.
     */
    public void computeCommonWords(){
        this.commonWords = this.doc1.getKeys();
        this.commonWords.retainAll((this.doc2.getKeys()));
    }

    public Set<String> getCommonWords(){
        return this.commonWords;
    }

    /***
     * get combination of two common words.
     */
    public void computeCommonWordCombination(){
        this.cwCombination = new ArrayList<>();
        ArrayList<String> cw = new ArrayList<>(this.commonWords);
        for (int i = 0; i < cw.size() - 1; i++){
            for (int j = i + 1; j < cw.size(); j++){
                this.cwCombination.add(new Pair<>(cw.get(i), cw.get(j)));
            }
        }
    }

    public void computeJaccardSimilarity(){
        this.jaccardSimilarity = (double) this.commonWords.size() / (this.doc1.getKeys().size() + this.doc2.getKeys().size() - this.commonWords.size());
    }

    public Double getJaccardSimilarity(){
        return this.jaccardSimilarity;
    }

    /***
     * compute diameter of semantic graph as denominator of weight.
     * @param graph
     * @return
     */
    public static Integer[] getDiameter(SemanticGraph graph){
        return getDiameter(graph, graph.getFirstRoot());
    }

    public static Integer[] getDiameter(SemanticGraph graph, IndexedWord root){
        Integer DandH[] = {1,1};

        if(graph.hasChildren(root)){

            Set<IndexedWord> children = graph.getChildren(root);

            List<Integer> childrenHeight = new ArrayList<>();
            List<Integer> childrenDiameter = new ArrayList<>();

            children.forEach(child->{
                childrenHeight.add(getDiameter(graph, child)[0]);
                childrenDiameter.add(getDiameter(graph, child)[1]);
            });

            Integer largest = Collections.max(childrenHeight);

            if(children.size() > 1) {
                childrenHeight.remove(largest);
            }

            Integer height = largest + 1;
            Integer rootDiameter = height;

            rootDiameter += Collections.max(childrenHeight);

            Integer finalDiameter = Math.max(Collections.max(childrenDiameter), rootDiameter);

            DandH[0] = height;
            DandH[1] = finalDiameter;
        }
        return DandH;
    }

    /***
     * compute sentence weight with diameter.
     * @param diameter
     * @param distance
     * @return
     */
    public static Double computeWeight(Integer diameter, Integer distance){
        return 1 - (double)distance / diameter;
    }

    /***
     * compute sentence weight without diameter.
     * @param distance
     * @return
     */
    public static Double computeWeight(Integer distance){
        return (double)2/distance;
    }

    /***
     * Core algorithm: compute sentences weight based on one pair of combination of common words.
     * @param wordPair
     * @param mani
     * @return
     */
    public ArrayList<Double> getSentencesWeights(Pair<String, String> wordPair, Mani mani) {
        if (!mani.getKeys().contains(wordPair.first()) || !mani.getKeys().contains(wordPair.second())) {
            System.out.println("Common Words Error!");
            return null;
        } else {

            //for test only!
            for (Map.Entry<IndexedWord, SemanticGraph> entry : mani.getSentences(wordPair.first()).entrySet()) {
                this.word1SentMap = new HashMap<>();
                this.word1SentMap.put(entry.getKey(), entry.getKey().sentIndex());
            }

            for (Map.Entry<IndexedWord, SemanticGraph> entry: mani.getSentences(wordPair.second()).entrySet()){
                this.word2SentMap = new HashMap<>();
                this.word2SentMap.put(entry.getKey(), entry.getKey().sentIndex());
            }
            //

            LinkedHashMap<IndexedWord, SemanticGraph> comparer;
            LinkedHashMap<IndexedWord, SemanticGraph> comparee;

            ArrayList<Double> weights = new ArrayList<>();

            if (mani.getSentences(wordPair.first()).size() >= mani.getSentences(wordPair.second()).size()) {
                comparer = mani.getSentences(wordPair.first());
                comparee = mani.getSentences(wordPair.second());
            } else {
                comparer = mani.getSentences(wordPair.second());
                comparee = mani.getSentences(wordPair.first());
            }
//            System.out.println(wordPair.first() + ' ' + wordPair.second());
            for (Map.Entry<IndexedWord, SemanticGraph> entryer : comparer.entrySet()) {
                for (Map.Entry<IndexedWord, SemanticGraph> entryee : comparee.entrySet()) {
//                    System.out.println("SentID:" + entryer.getKey().sentIndex() + ' ' + entryee.getKey().sentIndex());
                    if (entryer.getKey().sentIndex() < entryee.getKey().sentIndex()) {
                        break;
                    } else if (entryer.getKey().sentIndex() == entryee.getKey().sentIndex()) {
//                        weights.add(
//                                computeWeight(
//                                        getDiameter(entryer.getValue())[1],
//                                        entryer.getValue().getShortestUndirectedPathNodes(
//                                                entryer.getKey(),
//                                                entryee.getKey()).size()));
                        Double w = computeWeight(entryer.getValue().getShortestUndirectedPathNodes(
                                entryer.getKey(),
                                entryee.getKey()).size());
                        weights.add(w);

//                        weights.add(computeWeight(entryer.getValue().getShortestUndirectedPathNodes(
//                                entryer.getKey(),
//                                entryee.getKey()).size()));
//                        System.out.println("Weight:" + w);
                    }
                }
            }
            return weights;
        }
    }

    public static Double similarity(ArrayList<Double> doc1Weights, ArrayList<Double> doc2Weights){
        return doc1Weights.stream().mapToDouble(Double::doubleValue).sum() * doc2Weights.size() +
                doc2Weights.stream().mapToDouble(Double::doubleValue).sum() * doc1Weights.size();
    }

    public Double getSimilarity(){
        this.pairWeights = new ArrayList<>();
        this.weights = new HashMap<>();

        for (Pair<String, String> pair: this.cwCombination){
            ArrayList<Double> doc1Weights = getSentencesWeights(pair, this.doc1);
            ArrayList<Double> doc2Weights = getSentencesWeights(pair, this.doc2);
            this.weights.put(pair, new ArrayList[]{doc1Weights, doc2Weights});
//            System.out.println(similarity(doc1Weights, doc2Weights));
            this.pairWeights.add(similarity(doc1Weights, doc2Weights));
        }
//        System.out.println(this.pairWeights);
//        System.out.println(this.commonWords.size());
//        System.out.println(this.pairWeights.size());
        return this.pairWeights.stream().mapToDouble(Double::doubleValue).sum();
    }

    public static boolean isStopword(IndexedWord indexedWord){
        Pair<Boolean, Boolean> stopword = indexedWord.backingLabel().get(StopwordAnnotator.class);
        if (stopword.first() || stopword.second()){
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args){
        System.out.println(computeWeight(3,2));
        System.out.println(computeWeight(3));
        System.out.println(similarity(new ArrayList<>(), new ArrayList<>()));
    }


}
