import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mike on 11/1/17.
 */
public class ProjectRunner2 {

    private static final File folder = new File("/home/mike/Documents/corpus/orig/");

    public static void main(String[] args) throws FileNotFoundException {
        Briefcase briefcase = new Briefcase(folder);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        System.out.println("Start!");

        PrintStream out = new PrintStream(new FileOutputStream("/home/mike/Desktop/ManiOutput2.txt"));
        System.setOut(out);

        for (File[] filePair: briefcase.getAllFilePairs()){
            Runnable worker = new WorkerThread(filePair[0], filePair[1]);
            executor.execute(worker);
        }
        executor.shutdown();
    }

}
