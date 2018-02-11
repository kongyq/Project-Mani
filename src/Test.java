import java.util.ArrayList;
import java.util.Hashtable;

public class Test {
    /**
     * Returns argmax of an array of floats, 
     * argmax being the index of the maximum value. 
     * @param arr An array of float values 
     * @return index of maximum value 
     */
    private static Integer argmax(Float[] arr) {
        Float max = arr[0];
        Integer argmax = 0;
        for (int i=1; i<arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                argmax = i;
            }
        }
        return argmax;
    }

    /**
     * Returns the most probable analyzing path for a word, given an HMM 
     * @param word          Observation (word to analyze) 
     * @param transitions   Hash table describing probability of each 
     *                      HMM state to transition to the other states 
     * @param outputs       Hash table describing probability of an output 
     *                      character in each state 
     * @return An array of state indexes passed in the most probably analyzing 
     */
    public static ArrayList<Integer> viterbiPath(String word,
                                                 Hashtable<Integer, Hashtable<Integer, Float>> transitions,
                                                 Hashtable<Integer, Hashtable<Character, Float>> outputs) {

        int T = word.length(); // word length 
        int N = outputs.size(); // outputting states count (without q0, qF) 
        int F = N + 1; // final state index 

        // create a path probability table v : (N+2)xT 
        Float[][] v = new Float[N+2][T];
        Integer[][] bp = new Integer[N+2][T];

        // calculate first transition (first column in table) 
        for (int s=1; s<=N; s++) {
            v[s][0] = transitions.get(0).get(s) * outputs.get(s).get(word.charAt(0));
            bp[s][0] = 0;
        }

        // populate Viterbi table with max. probabilities 
        for (int t=1; t<T; t++) {
            for (int s=1; s<=N; s++) {
                Float[] probs = new Float[N];
                for (int i=1; i<=N; i++) {
                    if (v[i][t-1] != null) {
                        probs[i-1] = v[i][t-1] *
                                transitions.get(i).get(s) *
                                outputs.get(s).get(word.charAt(t));
                    }
                    else {
                        probs[i-1] = 0f;
                    }
                }
                int argmax = argmax(probs);
                bp[s][t] = argmax+1;
                v[s][t] = probs[argmax];
            }
        }

        // calculate transition to final state 
        Float[] probs = new Float[N];
        for (int i=1; i<=N; i++) {
            if (v[i][T-1] != null) {
                probs[i-1] = v[i][T-1] * transitions.get(i).get(F);
            }
            else {
                probs[i-1] = 0f;
            }
        }
        int argmax = argmax(probs);
        bp[F][T-1] = argmax+1;
        v[F][T-1] = probs[argmax];

        // return the backtrace path by following states from bp[F,T] backwards 
        ArrayList<Integer> path = new ArrayList<Integer>();
        int q = F;
        for (int i=T-1; i>=0; i--) {
            q = bp[q][i];
            path.add(0, q);
        }

        return path;
    }

    /**
     * Returns the total probability of an observation, given an HMM 
     * @param word          Observation (word to analyze) 
     * @param transitions   Hash table describing probability of each 
     *                      HMM state to transition to the other states 
     * @param outputs       Hash table describing probability of an output 
     *                      character in each state 
     * @return Total probability value of an observation 
     */
    public static Float viterbiForward(String word,
                                       Hashtable<Integer, Hashtable<Integer, Float>> transitions,
                                       Hashtable<Integer, Hashtable<Character, Float>> outputs) {

        int T = word.length(); // word length 
        int N = outputs.size(); // outputting states count (without q0, qF) 
        int F = N + 1; // final state index 

        // create a path probability table v : (N+2)xT 
        Float[][] alpha = new Float[N+2][T];

        // calculate first transition (first column in table) 
        for (int s=1; s<=N; s++) {
            alpha[s][0] = transitions.get(0).get(s) * outputs.get(s).get(word.charAt(0));
        }

        // populate Viterbi table with total probabilities 
        for (int t=1; t<T; t++) {
            for (int s=1; s<=N; s++) {
                alpha[s][t] = 0f;
                for (int i=1; i<=N; i++) {
                    if (alpha[i][t-1] != null) {
                        alpha[s][t]+= alpha[i][t-1] *
                                transitions.get(i).get(s) *
                                outputs.get(s).get(word.charAt(t));
                    }
                }
            }
        }

        // calculate transition to final state 
        alpha[F][T-1] = 0f;
        for (int i=1; i<=N; i++) {
            if (alpha[i][T-1] != null) {
                alpha[F][T-1]+= alpha[i][T-1] * transitions.get(i).get(F);
            }
        }

        // return final probability 
        return alpha[F][T-1];
    }

    /**
     * Defines the HMM we saw in class and prints the analyzing path 
     * calculated by viterbiPath() for a given word. 
     * @param args  Observations as a string (a word to analyze) 
     */
    public static void main(String[] args) {
        // prepare transition probabilities table 
        Hashtable<Integer, Hashtable<Integer, Float>> transitions =
                new Hashtable<Integer, Hashtable<Integer, Float>>();

        // final state 
        int F = 4;


        Hashtable<Integer, Float> q0 = new Hashtable<Integer, Float>();
        q0.put(1, 0.4f);
        q0.put(2, 0.4f);
        q0.put(3, 0.5f);
        Hashtable<Integer, Float> q1 = new Hashtable<Integer, Float>();
        q1.put(2, 0.6f);
        q1.put(3, 0.4f);
        Hashtable<Integer, Float> q2 = new Hashtable<Integer, Float>();
        q2.put(F, 0.6f);
        q2.put(3, 0.4f);
        Hashtable<Integer, Float> q3 = new Hashtable<Integer, Float>();
        q3.put(F, 0.5f);
        q3.put(1, 0.5f);
        transitions.put(0, q0);
        transitions.put(1, q1);
        transitions.put(2, q2);
        transitions.put(3, q3);

        // prepare outputs probabilities table 
        Hashtable<Integer, Hashtable<Character, Float>> outputs =
                new Hashtable<Integer, Hashtable<Character, Float>>();
        Hashtable<Character, Float> b1 = new Hashtable<Character, Float>();
        b1.put('A', 0.33f);
        b1.put('C', 0.16f);
        b1.put('G', 0f);
        b1.put('T', 0.5f);
        Hashtable<Character, Float> b2 = new Hashtable<Character, Float>();
        b2.put('A', 0.25f);
        b2.put('C', 0.25f);
        b2.put('G', 0.25f);
        b2.put('T', 0.25f);
        Hashtable<Character, Float> b3 = new Hashtable<Character, Float>();
        b3.put('A', 0f);
        b3.put('C', 0.1875f);
        b3.put('G', 0.0625f);
        b3.put('T', 0.75f);
        Hashtable<Character, Float> b4 = new Hashtable<Character, Float>();
        b4.put('A', 0.8f);
        b4.put('C', 0.1f);
        b4.put('G', 0.1f);
        b4.put('T', 0f);
        outputs.put(1, b1);
        outputs.put(2, b2);
        outputs.put(3, b3);
        outputs.put(4, b4);

        // validate input 

        if (args.length < 1) {
            System.err.println("Error! Please pass a word to analyze as an argument.");
        }
        else {
            // calculate requested values 
            ArrayList<Integer> path = viterbiPath(args[0], transitions, outputs);
            Float forward = viterbiForward(args[0], transitions, outputs);

            System.out.println("Input for Viterbi analysis:");
            System.out.println(args[0]);
            System.out.println("\nViterbi optimal path:");
            System.out.println(path);
            System.out.println("\nViterbi forward probability:");
            System.out.format("%.12f%n", forward);
        }
    }
}