import entity.NodeInfo;
import io.appium.java_client.android.AndroidDriver;
import org.dom4j.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppiumClient {
    private static final Logger logger = LoggerFactory.getLogger(AppiumClient.class);
    private static final Integer TIME_WAIT_LONG = 3000;
    private static final Integer TIME_WAIT_SHORT = 2000;
    private AndroidDriver<WebElement> driver = null;

    private void connectAppiumServer() throws MalformedURLException {
        logger.info("正在连接appiumServer");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "Mi 8 UD");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("platformVersion", "10.0");
        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        logger.info("连接成功");
    }

    public void run(String pkgName) throws IOException, InterruptedException, DocumentException {
        String filePre = "D:\\appium-apk\\";
        String pkgName1 = pkgName.split("_")[0];
        // 连接server
        connectAppiumServer();
        // 安装app
        logger.info("正在安装" + pkgName1);
        driver.installApp(filePre + pkgName);
        // adb赋权
        givePermission(pkgName1);
        //启动app
        logger.info("启动" + pkgName1);
        String command = "adb shell monkey -p " + pkgName1 + " -vvv 1";
        Runtime.getRuntime().exec(command);
        Thread.sleep(TIME_WAIT_LONG);
        // 启动页面识别
        runIntoMainPage();

    }

    private void searchAllElements(Element element, List<NodeInfo> nodeInfoList){
        Iterator itr = element.elementIterator();
        while(itr.hasNext()){
            searchAllElements((Element) itr.next(), nodeInfoList);
        }
        Attribute attr = element.attribute("text");
        if(attr != null){
            String aStr = attr.getValue();
            if(!aStr.equals("")) {
                String clickable = element.attributeValue("clickable");
                String bounds = element.attributeValue("bounds");
                nodeInfoList.add(new NodeInfo(aStr, clickable, bounds));
            }
        }
    }

    private void runIntoMainPage() throws DocumentException {
        String pageSource = driver.getPageSource();
        Document document = DocumentHelper.parseText(pageSource);
        Element root = document.getRootElement();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        searchAllElements(root, nodeInfos);
        System.out.println(nodeInfos.toString());

        int a = 1;
    }

    private void givePermission(String pkgName) throws IOException {
        logger.info("正在赋权");
        String[] permissions = {
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "android.permission.LOCATION_POLICY_INTERNAL",
                "android.permission.READ_PHONE_STATE"
        };
        for(String permission: permissions){
            Runtime.getRuntime().exec("adb shell pm grant " + pkgName + " " + permission);
        }
    }

}
