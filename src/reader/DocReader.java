package reader;

import cleaner.Cleaner;
import com.google.common.base.CharMatcher;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.document.TextDocumentStatistics;
import de.l3s.boilerpipe.estimators.SimpleEstimator;
import de.l3s.boilerpipe.extractors.*;
import de.l3s.boilerpipe.filters.english.MinFulltextWordsFilter;
import de.l3s.boilerpipe.filters.english.NumWordsRulesClassifier;
import de.l3s.boilerpipe.filters.simple.MarkEverythingContentFilter;
import de.l3s.boilerpipe.filters.simple.MinWordsFilter;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.security.util.Length;

import javax.xml.soap.Text;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mike on 10/16/17.
 */
public class DocReader {
    private String title;
    private String text;
    private HashMap<Integer, String> shortArticles;


    public DocReader(){

    }

    public static String textClean(String rawText){

        return CharMatcher.whitespace().trimAndCollapseFrom(
                CharMatcher.ascii().retainFrom(
                        CharMatcher.javaIsoControl().removeFrom(rawText)),
                ' ');
    }

    public static String textClean2(String rawText){

        return CharMatcher.whitespace().trimAndCollapseFrom(
                CharMatcher.ascii().retainFrom(rawText),
                ' ');
    }

    public String getTitle(){
        return this.title;
    }

    public String getText(){
        return this.text;
    }

    public void readTxt(File txtFile) throws IOException {
        List<String> lines = Files.readAllLines(txtFile.toPath(), StandardCharsets.UTF_8);
        this.text = lines.toString();
    }

    public void read(File file) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        List<String> lines = Files.readAllLines(file.toPath(),charset);
        //lines.forEach(System.out::println);
        for (String line: lines){
            if (line.startsWith("title:")){
                this.title = line.substring(6);
            } else if (line.startsWith("source:")){
                continue;
            } else if (line.startsWith("text:")){
                this.text = line.substring(5);
            } else{
                this.text = this.text + ' ' + line;
            }
        }
        this.title = textClean(this.title);
        this.text = textClean(this.text);
    }

    public static TextDocument getTextDocument (File htmlFile) throws SAXException, BoilerpipeProcessingException, FileNotFoundException {
        FileReader reader = new FileReader(htmlFile);
        InputSource is = new InputSource(reader);
        return new BoilerpipeSAXInput(is).getTextDocument();
    }

    public void readHtml(File htmlFile) throws FileNotFoundException, SAXException, BoilerpipeProcessingException {

        TextDocument docArticle = getTextDocument(htmlFile);
        TextDocument docCanola = getTextDocument(htmlFile);
        TextDocument docDefault = getTextDocument(htmlFile);
//        TextDocumentStatistics before = new TextDocumentStatistics(getTextDocument(htmlFile), false);

        ArticleExtractor.INSTANCE.process(docArticle);
        DefaultExtractor.INSTANCE.process(docDefault);
        CanolaExtractor.INSTANCE.process(docCanola);

//        TextDocumentStatistics afterA = new TextDocumentStatistics(docArticle, true);
//        TextDocumentStatistics afterC = new TextDocumentStatistics(docCanola, true);
//        TextDocumentStatistics afterD = new TextDocumentStatistics(docDefault, true);

        String[] articleContent = {detectNull(docArticle.getContent()), "Article"};
        String[] canolaContent = {detectNull(docCanola.getContent()), "Canola"};
        String[] defaultContent = {detectNull(docDefault.getContent()), "Default"};

//        System.out.format("A: %s\t C: %s\t D: %s\n",
//                SimpleEstimator.INSTANCE.isLowQuality(before, afterA),
//                SimpleEstimator.INSTANCE.isLowQuality(before, afterC),
//                SimpleEstimator.INSTANCE.isLowQuality(before, afterD));
//        FileReader reader = new FileReader(htmlFile);
//        String[] articleContent = {ArticleExtractor.INSTANCE.getText(reader), "Article"};
//        reader = new FileReader(htmlFile);
//        String[] defaultContent = {DefaultExtractor.INSTANCE.getText(reader), "Default"};
//        reader = new FileReader(htmlFile);
//        String[] canolaContent = {CanolaExtractor.INSTANCE.getText(reader), "Canola"};

//        System.out.println(articleContent[0]);
//        System.out.println("========================================================================================");
//        System.out.println(defaultContent[0]);
//        System.out.println("========================================================================================");
//        System.out.println(canolaContent[0]);
//
//        InputSource is = new InputSource(reader);
//        // parse the document into boilerpipe's internal data structure
//        TextDocument doc = new BoilerpipeSAXInput(is).getTextDocument();
//        TextDocument docDefault = doc;
//        TextDocument docArticle = doc;
//        TextDocument docCanola = doc;
//        // perform the extraction/classification process on "doc"
////        DefaultExtractor.INSTANCE.process(docDefault);
////        CanolaExtractor.INSTANCE.process(docCanola);
////        KeepEverythingWithMinKWordsExtractor kewmkwe = new KeepEverythingWithMinKWordsExtractor(10);
////        kewmkwe.process(doc);
//        ArticleExtractor.INSTANCE.process(docArticle);
//
////        ArticleSentencesExtractor.INSTANCE.process(doc);
////        MarkEverythingContentFilter.INSTANCE.process(doc);
////        MinWordsFilter mwf = new MinWordsFilter(5);
////        mwf.process(docDefault);
////        mwf.process(docCanola);
////        mwf.process(docArticle);
//          //*********************
        String[][] candidates = {articleContent, defaultContent, canolaContent};
        //************************

////        String[] titleCandidates = {docDefault.getTitle(), docCanola.getTitle(), docArticle.getTitle()};
//
////        MinFulltextWordsFilter.DEFAULT_INSTANCE.process(docArticle);
////        MinFulltextWordsFilter filter = new MinFulltextWordsFilter(1);
////        filter.process(docDefault);
////        NumWordsRulesClassifier.INSTANCE.process(docCanola);
//        this.text = articleContent;
////        for (TextBlock tb: docCanola.getTextBlocks()){
////            System.out.println(tb.getNumWords());
////            System.out.println(tb.isContent());
////            System.out.println(tb.getText());
////        }
        String[] temp = Arrays.stream(candidates).max(Comparator.comparing((String[] doc) -> doc[0].length())).get();
//        this.text = Arrays.stream(candidates).max(Comparator.comparing((String[] doc) -> doc[0].length())).get()[0];
        this.text = textClean2(temp[0]);
//        this.text = textClean2(articleContent[0]);
//        System.out.println(temp[1]);

////        this.text = doc.getText(true, true);
//        this.title = docArticle.getTitle();
////        this.title = Arrays.stream(titleCandidates).max(Comparator.comparing(String::length)).get();
    }

    public void readShortArticle(File shortArticleFile) throws IOException {
        this.shortArticles = new HashMap<>();
        List<String> lines = Files.readAllLines(shortArticleFile.toPath(), StandardCharsets.ISO_8859_1);
        String pattern = "\\(\\d+\\swords\\)";
        for (String line: lines){
//            System.out.println(line.split("\t")[0].replaceAll("\\.", ""));
            Integer index = Integer.parseInt(line.split("\t")[0].replaceAll("\\.", ""));
            String text = line.split("\t")[1].replaceAll(pattern, "");
            this.shortArticles.put(index, text);
        }
    }

    public HashMap<Integer, String> getShortArticles(){
        return this.shortArticles;
    }

    public static String detectNull(String orig){
        if (orig == null){
            return "";
        }else{
            return orig;
        }
    }

    public static void main(String[] arg) throws IOException, BoilerpipeProcessingException, SAXException {
        File file = new File("/home/mike/Documents/corpus/orig/cv/1.txt");
        File htmlFile = new File ("/home/mike/Documents/corpus/TmpCorpus/117_2.html");
        File txtFile = new File("/home/mike/Documents/corpus/TxtCorpus/117_2.txt");
        File shortArticleFile = new File("/home/mike/Documents/corpus/documents.txt");

//        System.out.println(file.isFile());
        DocReader dr = new DocReader();
//        dr.read(file);
        dr.readShortArticle(shortArticleFile);
//        dr.readHtml(htmlFile);
//        dr.readTxt(txtFile);
        System.out.println(dr.getShortArticles());
//        System.out.println(textClean("dewaewae wadwadwad a  de3 d3"));
//        System.out.println(dr.getText());
//        System.out.println(dr.getTitle());
//        Cleaner cleaner = new Cleaner();
//        cleaner.textClean(dr.getText());

//        System.out.println(cleaner.getRawText());
//        System.out.println(cleaner.getCleanedText());
//        System.out.println(dr.getTitle());
//        System.out.println(dr.getText());

    }
}
