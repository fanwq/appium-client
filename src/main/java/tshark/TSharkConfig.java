package tshark;

import com.huaban.analysis.jieba.JiebaSegmenter;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TSharkConfig {
    private List<String> excludeUrls = new ArrayList<>();
    private List<String> rules = new ArrayList<>();
    private String defaultInterface;
    private String appName;
    private String pkgName;

    public TSharkConfig(String appName, String pkgName) {
        this.appName = appName;
        this.pkgName = pkgName;
    }

    public void config() throws DocumentException, BadHanyuPinyinOutputFormatCombination {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File("src\\main\\resources\\TSharkConfig.xml"));
        Element root = document.getRootElement();
        Element defaultinterface = root.element("defaultinterface");
        defaultInterface = defaultinterface.getText();
        List excludeurls = root.element("excludeurls").elements();
        for(Object element:excludeurls){
            excludeUrls.add(((Element)element).getText());
        }
        Set<String> ruleSet = new HashSet<>();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        String sent = appName.split("-")[0];
        ruleSet.add(sent);
        ruleSet.add(pkgName);
        ruleSet.add(PinyinHelper.toHanYuPinyinString(sent, format, "", false));
        ruleSet.add(getInitials(sent));
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> words = segmenter.sentenceProcess(sent);
        for(String word : words){
            if(word.length() > 1){
                ruleSet.add(word);
                String ret = PinyinHelper.toHanYuPinyinString(word, format, "", false);
                String initials = getInitials(word);
                if(!ret.equals(""))
                    ruleSet.add(ret);
                if(!initials.equals("") && initials.length() > 1)
                    ruleSet.add(initials);
            }
        }
        rules.addAll(ruleSet);
        String[] pkgWords = pkgName.split("\\.");
        String[] nWords = {"android", "package", "com", "app", "cn", "ui", "mobile", "client"};
        for(String pkgWord:pkgWords){
            boolean isMatch = false;
            for(String nWord:nWords){
                if (pkgWord.equals(nWord)) {
                    isMatch = true;
                    break;
                }
            }
            if(isMatch) continue;
            if(!rules.contains(pkgWord))
                rules.add(pkgWord);
        }
    }

    private String getInitials(String word) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        char[] chars = word.toCharArray();
        StringBuilder sb = new StringBuilder();
        for(char c : chars){
            if(!(c > '0' && c < '9' || c > 'a' && c < 'z' || c > 'A' && c < 'Z'))
                sb.append(PinyinHelper.toHanYuPinyinString(String.valueOf(c), format, "", false).toCharArray()[0]);
        }
        return sb.toString();
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public String getDefaultInterface() {
        return defaultInterface;
    }

    public List<String> getRules() {
        return rules;
    }

    public String getAppName() {
        return appName;
    }

    public String getPkgName() {
        return pkgName;
    }
}
