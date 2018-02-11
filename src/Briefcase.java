import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 11/1/17.
 */
public class Briefcase {
    private List<File> allFiles;
    private List<File[]> allFilePairs;

    public Briefcase(File folder){
        this.allFiles = new ArrayList<>(find(folder));
        this.allFilePairs = new ArrayList<>(getFilePairs(this.allFiles));

    }

    public static List<File> find(File fullPath){
        List<File> fileList = (List<File>) FileUtils.listFiles(fullPath, new String[] {"txt"}, true);
        return fileList;
    }

//    public static List<File> findFiles(File folder) throws IOException {
//        Files.find(Paths.get(folder), 10, (p, bfa) -> bfa.isRegularFile()).forEach(System.out::println);
//    }

    public static List<File[]> getFilePairs (List<File> files){
        List<File[]> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i ++){
            for (int j = i+1; j< files.size(); j ++){
                result.add(new File[] {files.get(i), files.get(j)});
            }
        }
        return result;
    }

    public List<File> getAllFiles(){
        return this.allFiles;
    }

    public List<File[]> getAllFilePairs(){
        return this.allFilePairs;
    }
}
