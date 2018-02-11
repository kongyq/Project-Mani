package tfidf;

/**
 * Created by mike on 7/8/17.
 */
import javax.json.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corenlp.CoreNlp;
import org.apache.commons.io.FileUtils;
import reader.DocReader;

/**
 * Class to read documents
 *
 * @author Mubin Shrestha
 */
public class CorpusParser {

    //This variable will hold all terms of each document in an array.
    public List<String> allTerms = new ArrayList<>(); //to hold all terms

    public HashMap<File, String[]> termsDocsArray = new HashMap<>();
    public HashMap<File, double[]> tfidfDocsVector = new HashMap<>();

    public static List<File> find(File fullPath){
        List<File> fileList = (List<File>) FileUtils.listFiles(fullPath, new String[] {"txt"}, true);
        return fileList;
    }

    /**
     * Method to read files and store in array.
     * @param filePath : source file path
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void parseFiles(File filePath) throws FileNotFoundException, IOException{
        List<File> allFiles = this.find(filePath);
        for (File f: allFiles){
//            System.out.println(f.getName());
            DocReader reader = new DocReader();
            reader.readTxt(f);

            CoreNlp coreNlp = new CoreNlp("stemmedtermsonly");
            coreNlp.read(reader.getText());
            coreNlp.createStemmedTerms();

            String[] tokenizedTerms = coreNlp.getStemmedTerms().toArray(new String[coreNlp.getStemmedTerms().size()]);

            for (String term : tokenizedTerms) {
                if (!allTerms.contains(term)) {  //avoid duplicate entry
                    allTerms.add(term);
                }
            }
            termsDocsArray.put(f, tokenizedTerms);

        }
    }

    /**
     * Method to create termVector according to its tfidf score.
     */
    public void tfIdfCalculator() {
        double tf; //term frequency
        double idf; //inverse document frequency
        double tfidf; //term requency inverse document frequency

        for (Map.Entry<File, String[]> entry: termsDocsArray.entrySet()){
            double[] tfidfvectors = new double[allTerms.size()];
            int count = 0;
            for (String terms: allTerms){
                tf = new TfIdf().tfCalculator(entry.getValue(), terms);
                idf = new TfIdf().idfCalculator(new ArrayList<>(termsDocsArray.values()), terms);
                tfidf = tf * idf;
                tfidfvectors[count] = tfidf;
                count ++;
            }
            tfidfDocsVector.put(entry.getKey(), tfidfvectors);
        }
    }

    /**
     * Method to calculate cosine similarity between all the documents.
     */
    public void getCosineSimilarity() {
        for (int i = 0; i < tfidfDocsVector.size(); i++) {
            for (int j = 0; j < tfidfDocsVector.size(); j++) {
                System.out.println("between " + i + " and " + j + "  =  "
                        + new CosineSimilarity().cosineSimilarity
                        (
                                tfidfDocsVector.get(i),
                                tfidfDocsVector.get(j)
                        )
                );
            }
        }
    }

    public double getCosineSimilarity(File doc1, File doc2){
        return new CosineSimilarity().cosineSimilarity(this.tfidfDocsVector.get(doc1),
                this.tfidfDocsVector.get(doc2));
    }


    private static final File TEST = new File("/home/mike/Documents/corpus/TxtCorpus/");

    public static void main(String[] arg){

        CorpusParser dp = new CorpusParser();
        try {
            dp.parseFiles(TEST);
            System.out.println("done");
            dp.tfIdfCalculator();
            System.out.println("doune");
            for (Map.Entry<File, double[]> entry: dp.tfidfDocsVector.entrySet()){
                System.out.println(entry.getKey().getName());
                System.out.println(entry.getValue().length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
