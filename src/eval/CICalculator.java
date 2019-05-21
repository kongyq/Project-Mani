package eval;

import edu.stanford.nlp.util.Pair;
import reader.EvalReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class CICalculator {

    private final File evalFile = new File("/home/anonymous/Documents/corpus/sim.csv");
    private EvalReader reader = new EvalReader();
    private Map<String, Double> simMap;
    private Map<String, Double> disMap;

    private Map<String, double[]> runSimMap;
    private Map<String, double[]> runDisMap;

    private int simSize;
    private int disSize;

    public CICalculator() throws IOException {
        this.reader.read(evalFile);
        this.simMap = new HashMap<>();
        this.disMap = new HashMap<>();


        for (Map.Entry<Pair<Integer, Integer>, Double> entry : this.reader.getSim().entrySet()) {
            int first = entry.getKey().first();
            int second = entry.getKey().second();
            if (first < second) {
                if(entry.getValue() <= 2.5d){
                    this.disMap.put(first + "," + second, entry.getValue());
                }
                if(entry.getValue() >= 3.5d){
                    this.simMap.put(first + "," + second, entry.getValue());
                }
            }
        }

        this.simSize = this.simMap.size();
        this.disSize = this.disMap.size();

        System.out.println(this.disSize);
        System.out.println(this.simSize);
    }

    // load run file and store similarities based on groups
    public void eval(File runFile) throws IOException {
        this.runDisMap = new HashMap<>();
        this.runSimMap = new HashMap<>();

        for(String line: Files.readAllLines(runFile.toPath())){
            String pair = line.split(":")[0].replaceAll("[^0-9,]+", " ").trim();
            double[] values = Arrays.stream(line.split(":")[1]
                    .replaceAll("[^0-9.]+", " ")
                    .trim()
                    .split(" "))
                    .mapToDouble(Double::valueOf)
                    .toArray();
            if(this.simMap.containsKey(pair)) this.runSimMap.put(pair, values);
            if(this.disMap.containsKey(pair)) this.runDisMap.put(pair, values);
        }
    }

    // calculate all means of similarities for given group
    public double[] mean(Map<String, double[]> runMap){
        double[] sumList = new double[21];
        for (double[] values : runMap.values()) {
            for (int i = 0; i < 21; i++) {
                sumList[i] += values[i];
            }
        }
        return Arrays.stream(sumList).map(v -> v / runMap.size()).toArray();
    }

    // calculate all std of similarities for given group
    public double[] std(Map<String, double[]> runMap) {
        double[] meanList = mean(runMap);
        double[] stdList = new double[21];
        for (double[] values : runMap.values()) {
            for (int i = 0; i < 21; i++) {
                stdList[i] += Math.pow(values[i] - meanList[i], 2);
            }
        }
        return Arrays.stream(stdList).map(v -> Math.sqrt(v / runMap.size())).toArray();
    }

    // calculate all ci of similarities for given group
    public double[] ci(Map<String,double[]> runMap, double Z) {
        double[] stdList = std(runMap);
        return Arrays.stream(stdList).map(v -> v / Math.sqrt(runMap.size()) * Z).toArray();
    }


    public List<Pair<Integer, Integer>> misCounter(double Z) {
        int[] simMisclassifiedList = new int[21];
        int[] disMisclassifiedList = new int[21];

        double[] simCiList = ci(this.runSimMap, Z);
        double[] disCiList = ci(this.runDisMap, Z);

        double[] simMeanList = mean(this.runSimMap);
        double[] disMeanList = mean(this.runDisMap);

        double[] disSupremum = new double[21];
        double[] simInfimum = new double[21];

        for (int i = 0; i < 21; i++) {
            disSupremum[i] = disMeanList[i] + disCiList[i];
            simInfimum[i] = simMeanList[i] - simCiList[i];
        }

        List<Pair<Integer, Integer>> res = new ArrayList<>(21);

        for (double[] values : this.runDisMap.values()) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] > simInfimum[i]) {
                    disMisclassifiedList[i]++;
                }
            }
        }

        for (double[] values : this.runSimMap.values()) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] < disSupremum[i]) {
                    simMisclassifiedList[i]++;
                }
            }
        }

        for (int i = 0; i < 21; i++) {
            res.add(new Pair<>(disMisclassifiedList[i], simMisclassifiedList[i]));
        }
        return res;
    }

    public boolean validation(){
        for (double[] values : this.runSimMap.values()) {
            if (values.length != 21) return false;
        }
        for (double[] values : this.runDisMap.values()) {
            if (values.length != 21) return false;
        }
        if(this.runSimMap.size() != 46 || this.runDisMap.size() != 1095) return false;
        return true;
    }

    public void showTPTN(double disSupremum, double simInfimum, int index) {
        int dis = 0;
        int sim = 0;
        for (double[] values : this.runDisMap.values()) {
            if (values[index] <= disSupremum) {
                dis++;
            }
        }
        for (double[] values : this.runSimMap.values()) {
            if (values[index] >= simInfimum) {
                sim++;
            }
        }
        System.out.println(dis+","+sim);
    }

    public List<Pair<Float, Float>> calculateER(List<Pair<Integer, Integer>> misclassifiedMap) {
        List<Pair<Float, Float>> res = new ArrayList<>(21);
        for (Pair<Integer, Integer> pair : misclassifiedMap) {
            res.add(new Pair<>((float)pair.first / this.disSize, (float)pair.second / this.simSize));
        }
        return res;
    }

    public void showCorrectNumber(int index, double Z) {
        int[] simMisclassifiedList = new int[21];
        int[] disMisclassifiedList = new int[21];

        double[] simCiList = ci(this.runSimMap, Z);
        double[] disCiList = ci(this.runDisMap, Z);

        double[] simMeanList = mean(this.runSimMap);
        double[] disMeanList = mean(this.runDisMap);

        double disSupremum = disMeanList[index] + disCiList[index];
        double simInfimum = simMeanList[index] - simCiList[index];

        int correctSim = 0;
        int correctDis = 0;

        for (double[] values : this.runDisMap.values()) {
            if (values[index] <= disSupremum) {
                correctDis++;
            }
        }

        for (double[] values : this.runSimMap.values()) {
            if (values[index] >= simInfimum) {
                correctSim++;
            }
        }

        System.out.println(correctDis + "," + correctSim);
    }

    public static void main(String[] args) throws IOException {
        CICalculator calculator = new CICalculator();
        calculator.eval(new File("/home/anonymous/Documents/ManiOutput_Babel_ADW_ALLPOS_NOSW_FULL.txt"));
        System.out.println(calculator.validation());
        calculator.misCounter(2.576).forEach(System.out::println);
        List<Pair<Float, Float>> res = calculator.calculateER(calculator.misCounter(2.576));
        for (Pair<Float, Float> pair : res) {
            System.out.format("%2.2f,%2.2f\n", pair.first*100, pair.second*100 );
        }
        res.forEach(System.out::println);
        calculator.showCorrectNumber(12, 2.576);
        calculator.showTPTN(2.26080808252, 10.427290858, 12);
    }







}
