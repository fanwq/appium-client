import appium.ApkDownloader;
import appium.AppiumClient;
import entity.ThreadFinish;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tshark.TShark;

import java.io.IOException;

public class MainProcess {
    private static final Logger logger = LoggerFactory.getLogger(MainProcess.class);
    private static final Object lock = new Object();
    private static ThreadFinish isFinish = new ThreadFinish(false);
    public static void main(String[] args) throws IOException {
        String appName = "蚂蚁借款";
        ApkDownloader apkDownloader = new ApkDownloader();
        String pkgName = apkDownloader.downloadApk(appName);
        if(pkgName.equals("")){
            logger.error("下载" + pkgName + "失败");
            System.exit(-1);
        }else{
            AppiumClient client = new AppiumClient(pkgName, lock, isFinish);
            Thread thread1 = new Thread(client);
            thread1.start();
        }

//        appium.AppiumClient client = new appium.AppiumClient();
//        client.run("com.tencent.news_6.2.30_6230.apk");
        TShark tShark = new TShark(appName, pkgName.split("_")[0], lock, isFinish);
        Thread thread2 = new Thread(tShark);
        thread2.start();

    }
}
