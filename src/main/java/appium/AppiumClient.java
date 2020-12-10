package appium;


import entity.NodeInfo;
import entity.ThreadFinish;
import enum1.AndroidKeyCode;
import exception.ADBException;
import io.appium.java_client.TouchAction;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import org.dom4j.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SwipUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AppiumClient implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AppiumClient.class);
    private String phoneWidth;
    private String phoneHeight;
    AndroidDriver<WebElement> driver;
    private final String pkgName;
    private final Object lock;
    private ThreadFinish threadFinish;
    private boolean isFirstParse;

    public AppiumClient(String pkgName, Object lock, ThreadFinish threadFinish, boolean isFirstParse) {
        this.pkgName = pkgName;
        this.lock = lock;
        this.threadFinish = threadFinish;
        this.isFirstParse = isFirstParse;
    }

    private void connectAppiumServer() throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("deviceName", "Mi 8 UD");
        desiredCapabilities.setCapability("platformName", "Android");
        desiredCapabilities.setCapability("platformVersion", "10.0");
        desiredCapabilities.setCapability("newCommandTimeout", 3600);
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"),desiredCapabilities);
    }

    public void run() {
        try{
            String filePre = "D:\\appium-apk\\";
            String pkgName1 = pkgName.split("_")[0];
            synchronized(this.lock){
                // 连接appium
                logger.info("正在连接appium...");
                connectAppiumServer();
//                logger.info("正在连接手机...");
//                if(!connectAdb()){
//                    logger.error("adb 连接失败！");
//                    System.exit(-1);
//                }
                // 安装app
                if(isFirstParse){
                    logger.info("正在安装" + pkgName1);
                    if(!installApp(filePre + pkgName)){
                        logger.error("app安装失败");
                        System.exit(-1);
                    }
                }
                //启动app
                logger.info("启动" + pkgName1);
                openApp(pkgName1);
                this.lock.wait();
            }
            Thread.sleep(3000);
            parseWidthAndHeight();
            // 启动页面识别
            logger.info("启动页面识别");
            runIntoMainPage();
            //主页面识别
            logger.info("主页面识别");
            runMainPage(pkgName1);
            threadFinish.setFinish(true);
            pressKey(AndroidKeyCode.HOME);
            driver.quit();
            logger.info("appium结束");
        } catch (InterruptedException | IOException | DocumentException | ADBException e) {
            e.printStackTrace();
        }


    }

    private List<String> execAdbCmd(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        List<String> lineList = new ArrayList<>();
        while((line = br.readLine()) != null){
            lineList.add(line);
        }
        return lineList;
    }

    private void parseWidthAndHeight() throws IOException, DocumentException {
//        List<String> lineList = execAdbCmd("adb shell wm size");
//        phoneWidth = lineList.get(0).split(":")[1].trim().split("x")[0];
//        phoneHeight = lineList.get(0).split(":")[1].trim().split("x")[1];
        String ps = driver.getPageSource();
        Document document = DocumentHelper.parseText(ps);
        Element element = document.getRootElement();
        phoneWidth = element.attributeValue("width");
        phoneHeight = element.attributeValue("height");
    }

    private boolean connectAdb() throws IOException {
        List<String> lineList = execAdbCmd("adb devices");
        return lineList.size() > 1 && lineList.get(1).split(" ")[1].equals("device") && lineList.get(1).split(" ")[0].equals("2bb225c");
    }

    private boolean installApp(String pkgName) throws IOException {
        List<String> lineList = execAdbCmd("adb install -r -g " + pkgName);
        return lineList.size() == 1 && lineList.get(0).equals("Success");
    }

    private void openApp(String pkgName) throws IOException {
        String command = "adb shell monkey -p " + pkgName + " -vvv 1";
        Runtime.getRuntime().exec(command);
    }

//    private String currentActivity() throws IOException, ADBException {
//        String command = "adb shell dumpsys window";
//        List<String> list = execAdbCmd(command);
//        for(String line:list){
//            if(line.contains("mCurrentFocus")){
//                System.out.println(line);
//                return line.split(" ")[4].split("/")[1].split("}")[0];
//            }
//        }
//        throw new ADBException("获取当前activity失败");
//    }

//    private String getCurrentPackage() throws IOException, ADBException {
//        String command = "adb shell dumpsys window";
//        List<String> list = execAdbCmd(command);
//        for(String line:list){
//            if(line.contains("mCurrentFocus")){
//                return line.split(" ")[4].split("/")[0];
//            }
//        }
//        throw new ADBException("获取当前package失败");
//    }

    private void pressKey(AndroidKeyCode key) throws IOException {
        execAdbCmd("adb shell input keyevent " + key.getCode());
    }

    private void runMainPage(String pkgName1) throws DocumentException, IOException, InterruptedException, ADBException {
        pressKey(AndroidKeyCode.BACK);
        Thread.sleep(200);
        if(driver.currentActivity().equals(".launcher.Launcher") && driver.getCurrentPackage().equals("com.miui.home")){
            String command = "adb shell monkey -p " + pkgName1 + " -vvv 1";
            Runtime.getRuntime().exec(command);
            Thread.sleep(3000);
            Document document = DocumentHelper.parseText(driver.getPageSource());
            List<NodeInfo> nodeInfos = new ArrayList<>();
            //识别出页面中所有课点击的控件
            searchAllElementsClickable(document.getRootElement(), nodeInfos);
            skipProtocol(nodeInfos);
        }
        Document document = DocumentHelper.parseText(driver.getPageSource());
        List<NodeInfo> nodeInfos = new ArrayList<>();
        //识别出页面中所有课点击的控件
        searchAllElementsClickable(document.getRootElement(), nodeInfos);
        //遍历控件，识别出底部tabs
        List<String> bottomTabs = findBottomTabs(nodeInfos);
        for(String botTab:bottomTabs){
            tapUtils(botTab);
            String curActivity = driver.currentActivity();
            String curPackage = driver.getCurrentPackage();
            logger.info("base:" + curActivity + "," + curPackage);
            if (detectLoginPage(curActivity, curPackage)) continue;
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
                logger.info("tap:" + act + "," + pkg);
                if (detectLoginPage(curActivity, curPackage)) continue;
                if(act.equals(curActivity) && pkg.equals(curPackage)){
                    List<NodeInfo> tempInfos = new ArrayList<>();
                    boolean isWindow = false;
                    searchAllElementsClickable(DocumentHelper.parseText(driver.getPageSource()).getRootElement(), tempInfos);
                    for(NodeInfo info : tempInfos){
                        if(info.getResourceId().contains("close")){
                            driver.findElementById(info.getResourceId()).click();
                            isWindow = true;
                            break;
                        }
                    }
                    if(!isWindow){
                        String bounds = dealTopTabs(topTabs, node.getBounds());
                        if(!bounds.equals(""))
                            tapUtils(bounds);
                    }
                }
                pressKey(AndroidKeyCode.BACK);
                while(!(driver.getCurrentPackage().equals(curPackage) && driver.currentActivity().equals(curActivity))){
                    pressKey(AndroidKeyCode.BACK);
                    if(driver.currentActivity().equals(".launcher.Launcher") && driver.getCurrentPackage().equals("com.miui.home")){
                        String command = "adb shell monkey -p " + pkgName1 + " -vvv 1";
                        Runtime.getRuntime().exec(command);
                    }
                    Thread.sleep(200);
                }

            }
        }

    }

    private String dealTopTabs(List<String> topTabs, String bounds){
        if(topTabs.size() == 0){
            topTabs.add(bounds);
            return "";
        }
        String lastBounds = topTabs.get(topTabs.size() - 1);
        String[] tList = lastBounds.replace("[", "").replace("]", ",").split(",");
        String tStr1 = tList[1] + "," + tList[3];
        tList = bounds.replace("[", "").replace("]", ",").split(",");
        String tStr2 = tList[1] + "," + tList[3];
        if(tStr1.equals(tStr2)){
            topTabs.add(bounds);
        }else{
            topTabs.clear();
            topTabs.add(bounds);
        }
        return topTabs.get(0);
    }

    private boolean detectLoginPage(String curActivity, String curPackage) throws DocumentException, InterruptedException, IOException, ADBException {
        String activity = driver.currentActivity();
        if(activity.toLowerCase().contains("login")){
            Document doc = DocumentHelper.parseText(driver.getPageSource());
            List<NodeInfo> nodeInfoList = new ArrayList<>();
            //识别出页面中所有课点击的控件
            searchAllElements(doc.getRootElement(), nodeInfoList);
            skipProtocol(nodeInfoList);
            while(!(driver.getCurrentPackage().equals(curPackage) && driver.currentActivity().equals(curActivity))){
                pressKey(AndroidKeyCode.BACK);
                Thread.sleep(200);
            }
            return true;
        }
        return false;
    }

    private List<String> findBottomTabs(List<NodeInfo> nodeInfos){
        if(nodeInfos == null) return null;
        List<String> ret = new ArrayList<>();
        List<NodeInfo> delNodes = new ArrayList<>();
        Map<Integer, String> map = new HashMap<>();
        for(NodeInfo node : nodeInfos){
            String[] bounds = node.getBounds().substring(1, node.getBounds().length() - 1).replaceAll("]\\[", ",").split(",");
            int yOff = (Integer.parseInt(bounds[1]) + Integer.parseInt(bounds[3])) / 2;
            int xOff = (Integer.parseInt(bounds[0]) + Integer.parseInt(bounds[2])) / 2;
            if(yOff > Integer.parseInt(phoneHeight)){
                map.put(xOff, node.getBounds());
                delNodes.add(node);
            }
        }
        Object[] keys = map.keySet().toArray();
        Arrays.sort(keys);
        for(Object key : keys){
            ret.add(map.get(key));
        }
        if(ret.size() % 2 == 1){
            ret.remove(ret.size()/2);
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

    private void runIntoMainPage() throws DocumentException, IOException, InterruptedException, ADBException {
        if(!preSkipProtocol()) return;
        swipProduction();
        if(!preSkipProtocol()) return;
        if(driver.currentActivity().toLowerCase().contains("login")){
            logger.info("识别到登录页，尝试通过返回键进入主界面");
            driver.hideKeyboard();
            String curPkg = driver.getCurrentPackage();
            pressKey(AndroidKeyCode.BACK);
            if(driver.getCurrentPackage().equals("com.miui.home") && driver.currentActivity().equals(".launcher.Launcher")){
                logger.info("失败，重新打开app");
                String command = "adb shell monkey -p " + curPkg + " -vvv 1";
                Runtime.getRuntime().exec(command);
            }
        }
        preSkipProtocol();
    }

    private void swipProduction() throws InterruptedException, DocumentException, IOException {
        Thread.sleep(3000);
        logger.info("划动函数");
        List<NodeInfo> nodeInfos = parsePageSourceWithoutDetect();
        skipProtocol(nodeInfos);
        while(nodeInfos == null || nodeInfos.size() == 0 || nodeInfos.size() == 1){
            if(nodeInfos == null || nodeInfos.size() == 0){
                SwipUtils.SwipeLeft(driver);
                logger.info("划");
            } else{
                tapUtils(nodeInfos.get(0).getBounds());
                break;
            }
            nodeInfos = parsePageSource();
        }
    }

    private boolean preSkipProtocol() throws DocumentException, InterruptedException, IOException, ADBException {
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

    private void skipProtocol(List<NodeInfo> nodeInfos) throws InterruptedException, IOException {
        String[] inKeywords = {"同意", "继续", "知道了", "跳过", "逛逛"};
        String[] outKeywords = {"不同意", "退出", "暂不使用"};
        String[] skipKeywords = {"close", "cancel", "skip", "del", "delete"};
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

    private void tapUtils(String boundsString) throws InterruptedException, IOException {
        String[] bounds = boundsString.substring(1, boundsString.length() - 1).replaceAll("]\\[", ",").split(",");
        int xL = Integer.parseInt(bounds[0]);
        int yL = Integer.parseInt(bounds[1]);
        int xR = Integer.parseInt(bounds[2]);
        int yR = Integer.parseInt(bounds[3]);
        int xOff = (xL + xR) / 2;
        int yOff = (yL + yR) / 2;
        logger.info("点击" + xOff + "," + yOff);
        if(xOff >= Integer.parseInt(this.phoneWidth) || yOff >= Integer.parseInt(this.phoneHeight)){
            String command = "adb shell input tap " + xOff + " " + yOff;
            Runtime.getRuntime().exec(command);
        }else{
            TouchAction action = new TouchAction(driver);
            PointOption pointOption = new PointOption();
            pointOption.withCoordinates(xOff,yOff);
            action.tap(pointOption).perform().release();
        }
        Thread.sleep(1000);
    }

    private void givePermission(String pkgName) throws IOException {
        logger.info("正在赋权");
        String[] permissions = {
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                //"android.permission.ACCESS_BACKGROUND_LOCATION",
                "android.permission.LOCATION_POLICY_INTERNAL",
                "android.permission.READ_PHONE_STATE",
                "android.permission.CAMERA"
        };
        for(String permission: permissions){
            Runtime.getRuntime().exec("adb shell pm grant " + pkgName + " " + permission);
        }
    }

}
