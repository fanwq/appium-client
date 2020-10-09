import com.huaban.analysis.jieba.JiebaSegmenter;
import entity.NodeInfo;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.dom4j.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Test {
    private int loopNum = 0;
    public static void main(String[] args) throws IOException {
        String cmd = "adb devices";
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while((line = br.readLine()) != null){
            System.out.println(line);
        }

    }


    private void searchAllElements(Element element, List<NodeInfo> nodeInfoList){
        Iterator itr = element.elementIterator();
        while(itr.hasNext()){
            loopNum++;
            System.out.println(loopNum);
            searchAllElements((Element) itr.next(), nodeInfoList);
        }
        Attribute attr = element.attribute("text");
        if(attr != null){
            String aStr = attr.getValue();
            String clickable = element.attributeValue("clickable");
            if(!aStr.equals("") || clickable.equals("true")) {
                Attribute resourceId = element.attribute("resource-id");
                String bounds = element.attributeValue("bounds");
                if(resourceId != null){
                    nodeInfoList.add(new NodeInfo(aStr, clickable, bounds, resourceId.getValue()));
                }else{
                    nodeInfoList.add(new NodeInfo(aStr, clickable, bounds, ""));
                }
            }
        }
    }
}
