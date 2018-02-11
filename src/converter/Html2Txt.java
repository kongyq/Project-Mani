package converter;

import briefcase.Briefcase;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import reader.DocReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by mike on 11/5/17.
 */
public class Html2Txt {

    private static final File HTMLFOLDER = new File("/home/mike/Documents/corpus/TmpCorpus/");
    private static final File TXTFOLDER = new File("/home/mike/Documents/corpus/TxtAonlyCorpus/");

    public static void main(String[] args) throws IOException, SAXException, BoilerpipeProcessingException {
        Briefcase briefcase = new Briefcase(HTMLFOLDER, "html");
        for (File file: briefcase.getAllFiles()){
            System.out.println("Converting " + file.toString());
            DocReader docReader = new DocReader();
            docReader.readHtml(file);
            saveFile(docReader.getText(),
                    Paths.get(TXTFOLDER.toString(), file.getName().toString().replace("html", "txt")).toFile());
        }



    }

    public static void saveFile(String content, File file) throws IOException {
        FileUtils.writeStringToFile(file, content, "UTF-8");
    }

}
