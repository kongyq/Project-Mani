package eval;

import edu.stanford.nlp.util.Pair;
import reader.EvalReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class CICalculator2 {

    private final File evalFile = new File("/home/anonymous/Documents/corpus/sim.csv");
    private EvalReader reader = new EvalReader();
    private Map<String, Double> simMap;
    private Map<String, Double> disMap;

    private Map<String, double[]> runSimMap;
    private Map<String, double[]> runDisMap;

    private int simSize;
    private int disSize;
//    private List<Double> meanList;

    public CICalculator2() throws IOException {
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
                    .replaceAll("[^0-9\\.]+", " ")
                    .trim()
                    .split(" "))
                    .mapToDouble(Double::valueOf)
                    .toArray();
            if(this.simMap.containsKey(pair)) this.runSimMap.put(pair, values);
            if(this.disMap.containsKey(pair)) this.runDisMap.put(pair, values);
        }
    }

    // calculate all means of similarities for given group
    public double[] simMean(){
        double[] sumList = new double[21];
        for (double[] values : this.runSimMap.values()) {
            for (int i = 0; i < 21; i++) {
                sumList[i] += values[i];
            }
        }
        return Arrays.stream(sumList).map(v -> v / this.simSize).toArray();
    }

    public double[] disMean() {
        double[] sumList = new double[21];
        int[] nonZeroList = new int[21];
        for (double[] values : this.runDisMap.values()) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] > 0d) {
                    sumList[i] += values[i];
                    nonZeroList[i] ++;
                }
//                sumList[i] += values[i];
            }
        }
        double[] res = new double[21];
        for (int i = 0; i < 21; i++) {
            res[i] = sumList[i] / nonZeroList[i];
        }
        return res;
    }

    // calculate all std of similarities for given group
    public double[] disStd() {
        double[] meanList = disMean();
        double[] stdList = new double[21];
        int[] nonZeroList= new int[21];
        for (double[] values : this.runDisMap.values()) {
            for (int i = 0; i < values.length; i++) {

                if (values[i] > 0d) {
                    stdList[i] += Math.pow(values[i] - meanList[i], 2);
                    nonZeroList[i] ++;
                }
//                stdList[i] += Math.pow(values[i] - meanList[i], 2);
            }
        }
        double[] res = new double[21];
        for (int i = 0; i < 21; i++) {
            res[i] = Math.sqrt(stdList[i] / nonZeroList[i]);
        }
        return res;
//        return Arrays.stream(stdList).map(v -> Math.sqrt(v / runMap.size())).toArray();
    }

    public double[] simStd() {
        double[] meanList = simMean();
        double[] stdList = new double[21];
        for (double[] values : this.runSimMap.values()) {
            for (int i = 0; i < 21; i++) {
                stdList[i] += Math.pow(values[i] - meanList[i], 2);
            }
        }
        return Arrays.stream(stdList).map(v -> Math.sqrt(v / this.simSize)).toArray();
    }

    public int[] validElem(Map<String, double[]> runMap) {
        int[] res = new int[21];
        for (double[] values : runMap.values()) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] > 0d) {
                    res[i]++;
                }
            }
        }
        return res;
    }

    // calculate all ci of similarities for given group
    public double[] disCi(double Z) {
        double[] stdList = disStd();
        int[] nonZeroList = validElem(this.runDisMap);
        double[] res = new double[21];
        for (int i = 0; i < 21; i++) {
            res[i] = stdList[i] / Math.sqrt(nonZeroList[i]) * Z;
        }
        return res;
//        return Arrays.stream(stdList).map(v -> v / Math.sqrt(runMap.size()) * Z).toArray();
    }

    public double[] simCi(double Z) {
        double[] stdList = simStd();
        return Arrays.stream(stdList).map(v -> v / Math.sqrt(this.simSize) * Z).toArray();
    }


    public List<Pair<Integer, Integer>> misCounter() {
        int[] simMisclassifiedList = new int[21];
        int[] disMisclassifiedList = new int[21];

        double[] simCiList = simCi(2.567);
        double[] disCiList = disCi(2.567);

        double[] simMeanList = simMean();
        double[] disMeanList = disMean();

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

    public static void main(String[] args) throws IOException {
        CICalculator2 calculator = new CICalculator2();
        calculator.eval(new File("/home/anonymous/Documents/ManiOutput_Babel_ADW_ALLPOS_NOSW_FULL.txt"));
        List<Pair<Integer, Integer>> res = calculator.misCounter();
        res.forEach(System.out::println);
    }







}
