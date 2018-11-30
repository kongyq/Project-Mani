package wordnet;

import edu.cmu.lti.jawjaw.pobj.POS;
import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.adw.textual.similarity.TextualSimilarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 2/20/18.
 */
public class Adw {

    private ADW pipeLine;
    private SignatureComparison measure;

    public Adw(){
        this.pipeLine = new ADW();
        this.measure = new WeightedOverlap();
    }

    public Double getBestMatchScore(List<String> senses1, List<String> senses2){
        Double score = 0D;
        for (String sense1: senses1){
            for (String sense2: senses2){
//                System.out.println(sense1 + " : " + sense2);
                Double subscore = this.getOffsetSimilarity(sense1, sense2);
                if (subscore > score){
                    score = subscore;
                }
            }
        }
        if (score >= 1D){
            return 1D;
        }else {
            return score;
        }
    }



    public Double getOffsetSimilarity(String word1, String word2){
        ItemType wordType = ItemType.SENSE_OFFSETS;
        return this.pipeLine.getPairSimilarity(
                word1.substring(0,8) + "-" + word1.substring(8),
                word2.substring(0,8) + "-" + word2.substring(8),
                DisambiguationMethod.ALIGNMENT_BASED,
                this.measure,
                wordType, wordType);

    }


    public Double getTextSimilarity(String text1, String text2){
        ItemType wordType = ItemType.SURFACE;
        return this.pipeLine.getPairSimilarity(text1,text2,DisambiguationMethod.ALIGNMENT_BASED, this.measure, wordType, wordType);
    }


    @SuppressWarnings("unchecked")
    public Double getSimilarity(String word1, String word2, POS pos){
//        if (!Arrays.asList(new String[] {"n", "v", "r", "a"}).contains(pos.name())){
//            System.out.println("EE");
//            return -1D;
//        }
        ItemType wordType = ItemType.SURFACE_TAGGED;
        return this.pipeLine.getPairSimilarity(
                word1.replace("#", "")+'#'+pos.name(),
                word2.replace("#", "")+'#'+pos.name(),
                DisambiguationMethod.ALIGNMENT_BASED,
                this.measure,
                wordType, wordType);
    }

    public Boolean isOOV(String word, POS pos){
        return TextualSimilarity.getInstance().isOOV(word, pos.name());
    }


    public static void main(String[] args)
    {
        Adw adw = new Adw();
//        System.out.println(adw.getBestMatchScore(Arrays.asList("00958896n","08256968n"), Arrays.asList("08256968n","00958896n")));
        System.out.println(adw.getOffsetSimilarity("14589223n", "00634906v"));
//        System.out.println(adw.getTextSimilarity("this is an apple.", "that is an orange."));
    }

    public static void main3(String[] args){
        ADW pipeLine = new ADW();

        //the two lexical items
        String text1 = "dog";
        String text2 = "cat";

        //types of the lexical items (set as auto-detect)
        ItemType text1Type = ItemType.SURFACE;
        ItemType text2Type = ItemType.SURFACE;

        //measure for comparing semantic signatures
        SignatureComparison measure = new WeightedOverlap();

        //calculate the similarity of text1 and text2
        double similarity = pipeLine.getPairSimilarity(
                text1, text2,
                DisambiguationMethod.ALIGNMENT_BASED,
                measure,
                text1Type, text2Type);

        //print out the similarity
        System.out.println(similarity);
    }
}
