import entity.NodeInfo;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.dom4j.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Test {
    private int loopNum = 0;
    public static void main(String[] args) throws MalformedURLException, DocumentException {
        AndroidDriver<WebElement> driver = null;
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "Mi 8 UD");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("platformVersion", "10.0");
        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        System.out.println(driver.getPageSource());
        Document document = DocumentHelper.parseText(driver.getPageSource());
        Element element = document.getRootElement();
        String width = element.attributeValue("width");
        String height = element.attributeValue("height");
        System.out.println(width + "," + height);
//        new Test().searchAllElements(element, nodeInfos);
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
