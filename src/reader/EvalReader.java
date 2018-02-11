package reader;

import edu.stanford.nlp.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mike on 11/23/17.
 */
public class EvalReader {

    private HashMap<Pair<Integer, Integer>, Double> sim;

    public EvalReader(){
        this.sim = new HashMap<>();
    }

    public void read(File evalFile) throws IOException {
        List<String> lines = Files.readAllLines(evalFile.toPath(), StandardCharsets.UTF_8);
        Integer col = 1;
        Integer row = 1;
        Integer index = 1;
        for (String line: lines){
            List<String> list = new ArrayList<String>(Arrays.asList(line.split(",")));
            for(String score: list){
                if (this.sim.containsKey(new Pair<>(row,col))){
                    continue;
                }else{
                    this.sim.put(new Pair<>(row, col), Double.valueOf(score));
                }
                col ++;
            }
            row ++;
            col = 1;
        }
    }

    public HashMap<Pair<Integer, Integer>, Double> getSim(){
        return this.sim;
    }

    public Double getScore(Pair<Integer, Integer> rowcol){
        if (!this.sim.containsKey(rowcol)){
            return null;
        }else{
            return this.sim.get(rowcol);
        }
    }

    public static void main(String[] args) throws IOException {
        File evalFile = new File("/home/mike/Documents/corpus/sim.csv");
        EvalReader evalReader = new EvalReader();
        evalReader.read(evalFile);
        System.out.println(evalReader.getSim());
        System.out.println(evalReader.getScore(new Pair<>(4,5)));
    }

}



