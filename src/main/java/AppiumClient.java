import entity.NodeInfo;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import org.dom4j.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SwipUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppiumClient {
    private static final Logger logger = LoggerFactory.getLogger(AppiumClient.class);
    private static final Integer TIME_WAIT_LONG = 5000;
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
            String clickable = element.attributeValue("clickable");
            if(!aStr.equals("") || clickable.equals("true")) {
                String bounds = element.attributeValue("bounds");
                nodeInfoList.add(new NodeInfo(aStr, clickable, bounds));
            }
        }
    }

    private void runIntoMainPage() throws DocumentException, InterruptedException {
        List<NodeInfo> nodeInfos = parsePageSource();
        while(!(nodeInfos.size() == 0 || nodeInfos.size() > 7
                || driver.currentActivity().toLowerCase().contains("login"))){
            skipProtocol(nodeInfos);
            nodeInfos = parsePageSource();
        }
        while(nodeInfos.size() == 0 || nodeInfos.size() == 1){
            if(nodeInfos.size() == 0){
                SwipUtils.SwipeLeft(driver);
            } else{
                tapUtils(nodeInfos.get(0).getBounds());
                Thread.sleep(TIME_WAIT_SHORT);
            }
            nodeInfos = parsePageSource();
        }
        nodeInfos = parsePageSource();
        while(!(nodeInfos.size() == 0 || nodeInfos.size() > 7
                || driver.currentActivity().toLowerCase().contains("login"))){
            skipProtocol(nodeInfos);
            nodeInfos = parsePageSource();
        }
    }

    private List<NodeInfo> parsePageSource() throws DocumentException {
        String pageSource = driver.getPageSource();
        Document document = DocumentHelper.parseText(pageSource);
        Element root = document.getRootElement();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        searchAllElements(root, nodeInfos);
        return nodeInfos;
    }

    private void skipProtocol(List<NodeInfo> nodeInfos) throws InterruptedException {
        String[] inKeywords = {"同意", "继续", "知道了", "跳过"};
        String[] outKeywords = {"不同意", "退出"};
        // String[] textKeywords = {"协议", "政策", "条款"};
        boolean isInButton = false;
        for(NodeInfo node : nodeInfos){
            boolean isOutButton = false;
            for(String outKeyword : outKeywords){
                if(node.getClickable().equals("true") && node.getText().contains(outKeyword)){
                    isOutButton = true;
                    break;
                }
            }
            if(isOutButton) continue;
            for(String inKeyword : inKeywords){
                if(node.getClickable().equals("true") && node.getText().contains(inKeyword)
                        && node.getText().length() < 15){
                    isInButton = true;
                    break;
                }
            }
            if(isInButton){
                logger.info("点击'" + node.getText() + "'");
                tapUtils(node.getBounds());
                break;
            }
        }

    }

    private void tapUtils(String boundsString) throws InterruptedException {
        String[] bounds = boundsString.substring(1, boundsString.length() - 1).replaceAll("]\\[", ",").split(",");
        int xL = Integer.parseInt(bounds[0]);
        int yL = Integer.parseInt(bounds[1]);
        int xR = Integer.parseInt(bounds[2]);
        int yR = Integer.parseInt(bounds[3]);
        int xOff = (xL + xR) / 2;
        int yOff = (yL + yR) / 2;
        TouchAction action = new TouchAction(driver);
        PointOption pointOption = new PointOption();
        pointOption.withCoordinates(xOff,yOff);
        action.tap(pointOption).perform().release();
        Thread.sleep(TIME_WAIT_SHORT);
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
