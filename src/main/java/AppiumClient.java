import entity.NodeInfo;
import entity.Page;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class AppiumClient {
    private static final Logger logger = LoggerFactory.getLogger(AppiumClient.class);
    private AndroidDriver<WebElement> driver = null;
    private Stack<Page> pageStack = new Stack<>();
    private String phoneWidth;
    private String phoneHeight;

    private void connectAppiumServer() throws MalformedURLException {
        logger.info("正在连接appiumServer");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "Mi 8 UD");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("platformVersion", "10.0");
        capabilities.setCapability("newCommandTimeout", "600");
        capabilities.setCapability("automationName", "UiAutomator2");
        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
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
        Thread.sleep(3000);
        Document document = DocumentHelper.parseText(driver.getPageSource());
        Element root = document.getRootElement();
        phoneWidth = root.attributeValue("width");
        phoneHeight = root.attributeValue("height");
        // 启动页面识别
        runIntoMainPage();
        //主页面识别
        runMainPage();

    }

    private void runMainPage() throws DocumentException, IOException, InterruptedException {
        Document document = DocumentHelper.parseText(driver.getPageSource());
        List<NodeInfo> nodeInfos = new ArrayList<>();
        //识别出页面中所有课点击的控件
        searchAllElementsClickable(document.getRootElement(), nodeInfos);
        //遍历控件，识别出底部tabs
        List<String> bottomTabs = findBottomTabs(nodeInfos);
        for(String botTab:bottomTabs){
            adbTapUtils(botTab);
            String curActivity = driver.currentActivity();
            String curPackage = driver.getCurrentPackage();
            document = DocumentHelper.parseText(driver.getPageSource());
            nodeInfos = new ArrayList<>();
            List<String> topTabs = new ArrayList<>();
            //识别出页面中所有课点击的控件
            searchAllElementsClickable(document.getRootElement(), nodeInfos);
            findBottomTabs(nodeInfos);
            for(NodeInfo node : nodeInfos){
                tapUtils(node.getBounds());
                String pkg = driver.getCurrentPackage();
                String act = driver.currentActivity();
                if(act.toLowerCase().contains("login")){
                    Document doc = DocumentHelper.parseText(driver.getPageSource());
                    List<NodeInfo> nodeInfoList = new ArrayList<>();
                    //识别出页面中所有课点击的控件
                    searchAllElements(doc.getRootElement(), nodeInfoList);
                    skipProtocol(nodeInfoList);
                    while(!(driver.getCurrentPackage().equals(curPackage) && driver.currentActivity().equals(curActivity))){
                        driver.pressKey(new KeyEvent(AndroidKey.BACK));
                        Thread.sleep(200);
                    }
                    continue;
                }
                if(act.equals(curActivity) && pkg.equals(curPackage)){
                    topTabs.add(node.getBounds());
                }
                while(!(driver.getCurrentPackage().equals(curPackage) && driver.currentActivity().equals(curActivity))){
                    driver.pressKey(new KeyEvent(AndroidKey.BACK));
                    Thread.sleep(200);
                }

            }
        }

    }

    private List<String> findBottomTabs(List<NodeInfo> nodeInfos){
        if(nodeInfos == null) return null;
        List<String> ret = new ArrayList<>();
        List<NodeInfo> delNodes = new ArrayList<>();
        for(NodeInfo node : nodeInfos){
            String[] bounds = node.getBounds().substring(1, node.getBounds().length() - 1).replaceAll("]\\[", ",").split(",");
            int yOff = (Integer.parseInt(bounds[1]) + Integer.parseInt(bounds[3])) / 2;
            if(yOff > Integer.parseInt(phoneHeight)){
                ret.add(node.getBounds());
                delNodes.add(node);
            }
        }
        for(NodeInfo node: delNodes){
            nodeInfos.remove(node);
        }
        return ret;
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

    private void searchAllElementsClickable(Element element, List<NodeInfo> nodeInfoList){
        Iterator itr = element.elementIterator();
        while(itr.hasNext()){
            searchAllElementsClickable((Element) itr.next(), nodeInfoList);
        }
        String clickable = element.attributeValue("clickable");
        String bounds = element.attributeValue("bounds");
        String text = element.attributeValue("text");
        Attribute attr = element.attribute("resource-id");
        String resource_id = "";
        if(attr != null){
            resource_id = attr.getValue();
        }
        if(clickable != null && bounds != null && clickable.equals("true") && !resource_id.contains("search"))
            nodeInfoList.add(new NodeInfo(text, clickable, bounds, resource_id));
    }

    private void runIntoMainPage() throws DocumentException, IOException, InterruptedException {
        if(!preSkipProtocol()) return;
        swipProduction();
        if(!preSkipProtocol()) return;
        if(driver.currentActivity().toLowerCase().contains("login")){
            logger.info("识别到登录页，尝试通过返回键进入主界面");
            driver.hideKeyboard();
            String curPkg = driver.getCurrentPackage();
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            if(driver.getCurrentPackage().equals("com.miui.home") && driver.currentActivity().equals(".launcher.Launcher")){
                logger.info("失败，重新打开app");
                String command = "adb shell monkey -p " + curPkg + " -vvv 1";
                Runtime.getRuntime().exec(command);
            }
        }
        preSkipProtocol();
    }

    private void swipProduction() throws InterruptedException, DocumentException {
        Thread.sleep(3000);
        List<NodeInfo> nodeInfos = parsePageSourceWithoutDetect();
        skipProtocol(nodeInfos);
        while(nodeInfos == null || nodeInfos.size() == 0 || nodeInfos.size() == 1){
            if(nodeInfos == null || nodeInfos.size() == 0){
                SwipUtils.SwipeLeft(driver);
            } else{
                tapUtils(nodeInfos.get(0).getBounds());
                break;
            }
            nodeInfos = parsePageSource();
        }
    }

    private boolean preSkipProtocol() throws DocumentException, InterruptedException {
        List<NodeInfo> nodeInfos;
        int count = 0;
        NodeInfo t_node = null;
        if((nodeInfos = parsePageSource()) == null)
            return false;
        for(NodeInfo node: nodeInfos){
            if(node.getClickable().equals("true")) {
                t_node = node;
                count++;
            }
        }
        if(count == 1) tapUtils(t_node.getBounds());
        while(nodeInfos.size() <= 20){
            String frontAct = driver.currentActivity();
            skipProtocol(nodeInfos);
            String backAct = driver.currentActivity();
            if(frontAct.equals(backAct)) break;
            if((nodeInfos = parsePageSource()) == null)
                return false;
        }
        return true;
    }

    private List<NodeInfo> parsePageSource() throws DocumentException, InterruptedException {
        long time1 = System.currentTimeMillis();
        String pageSourceOld = driver.getPageSource();
        String pageSource = null;
        int count = 0;
        int timeOutCnt = 0;
        while(true) {
            long time2 = System.currentTimeMillis();
            if((time2 - time1) > 5000) return null;
            time1 = time2;
            pageSource = driver.getPageSource();
            timeOutCnt++;
            if(pageSourceOld.equals(pageSource)){
                count++;
            }else{
                count = 0;
            }
            if(count == 2) break;
            if(timeOutCnt > 4) break;
            pageSourceOld = pageSource;
            Thread.sleep(3000);
        }
        Document document = DocumentHelper.parseText(pageSource);
        Element root = document.getRootElement();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        searchAllElements(root, nodeInfos);
        return nodeInfos;
    }

    private List<NodeInfo> parsePageSourceWithoutDetect() throws DocumentException, InterruptedException {
        Document document = DocumentHelper.parseText(driver.getPageSource());
        Element root = document.getRootElement();
        List<NodeInfo> nodeInfos = new ArrayList<>();
        searchAllElements(root, nodeInfos);
        return nodeInfos;
    }

    private void skipProtocol(List<NodeInfo> nodeInfos) throws InterruptedException {
        String[] inKeywords = {"同意", "继续", "知道了", "跳过", "逛逛"};
        String[] outKeywords = {"不同意", "退出", "暂不使用"};
        String[] skipKeywords = {"close", "cancel", "skip"};
        // String[] textKeywords = {"协议", "政策", "条款"};
        boolean isInButton = false;
        for(NodeInfo node : nodeInfos){
            if(node.getText().equals("跳过")){
                tapUtils(node.getBounds());
                break;
            }
            boolean isOutButton = false;
            for(String outKeyword : outKeywords){
                if(node.getText().contains(outKeyword)){
                    isOutButton = true;
                    break;
                }
            }
            if(isOutButton) continue;
            for(String inKeyword : inKeywords){
                if(node.getText().contains(inKeyword)
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
            for(String skipWord:skipKeywords){
                if(node.getResourceId().contains(skipWord)){
                    tapUtils(node.getBounds());
                    break;
                }
            }

        }

    }

    private void tapUtils(String boundsString) throws InterruptedException {
        String[] bounds = boundsString.substring(1, boundsString.length() - 1).replaceAll("]\\[", ",").split(",");
        int xL = Integer.parseInt(bounds[0]);
        int yL = Integer.parseInt(bounds[1]);
        int xR = Integer.parseInt(bounds[2]);
        int yR = Integer.parseInt(bounds[3]);
        int xOff = Math.min((xL + xR) / 2, Integer.parseInt(this.phoneWidth) - 1);
        int yOff = Math.min((yL + yR) / 2, Integer.parseInt(this.phoneHeight) - 1);
        TouchAction action = new TouchAction(driver);
        PointOption pointOption = new PointOption();
        logger.info("点击" + xOff + "," + yOff);
        pointOption.withCoordinates(xOff,yOff);
        action.tap(pointOption).perform().release();
        Thread.sleep(1000);
    }

    private void adbTapUtils(String boundsString) throws IOException, InterruptedException {
        String[] bounds = boundsString.substring(1, boundsString.length() - 1).replaceAll("]\\[", ",").split(",");
        int xL = Integer.parseInt(bounds[0]);
        int yL = Integer.parseInt(bounds[1]);
        int xR = Integer.parseInt(bounds[2]);
        int yR = Integer.parseInt(bounds[3]);
        int xOff = (xL + xR) / 2;
        int yOff = (yL + yR) / 2;
        String command = "adb shell input tap " + xOff + " " + yOff;
        Runtime.getRuntime().exec(command);
        Thread.sleep(1000);
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
                "android.permission.READ_PHONE_STATE",
                "android.permission.CAMERA"
        };
        for(String permission: permissions){
            Runtime.getRuntime().exec("adb shell pm grant " + pkgName + " " + permission);
        }
    }

}
