package cleaner;

import com.google.common.base.CharMatcher;

/**
 * Created by mike on 10/17/17.
 */
public class Cleaner {

    private String rawText;
    private String cleanedText;

    public Cleaner(){

    }

    public void clean(String rawText){
        this.rawText = rawText;
        this.cleanedText = rawText.replaceAll("\\P{Print}", "");
    }

    public static String fClean(String rawText){

        return CharMatcher.whitespace().trimAndCollapseFrom(
                CharMatcher.ascii().retainFrom(
                        CharMatcher.javaIsoControl().removeFrom(rawText)),
                ' ');
    }

    public void textClean(String rawText){
        this.rawText = rawText;
        this.cleanedText = CharMatcher.ascii().retainFrom(rawText);
        this.cleanedText = CharMatcher.javaIsoControl().removeFrom(this.cleanedText);
        this.cleanedText = CharMatcher.whitespace().trimAndCollapseFrom(this.cleanedText, ' ');
        //this.cleanedText = CharMatcher.javaLetterOrDigit().retainFrom(rawText);
//        this.cleanedText = CharMatcher.invisible().removeFrom(rawText);
//        this.cleanedText = CharMatcher.javaLetterOrDigit().or(CharMatcher.whitespace()).retainFrom(rawText);
        //this.cleanedText = CharMatcher.javaIsoControl().removeFrom(rawText);
    }

    public String getCleanedText(){
        return this.cleanedText;
    }

    public String getRawText(){
        return this.rawText;
    }

    public void cleanHtml(String rawText){
        this.rawText = rawText;

    }
}
