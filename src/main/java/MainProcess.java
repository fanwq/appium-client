import appium.ApkDownloader;
import appium.AppiumClient;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tshark.TShark;

import java.io.IOException;

public class MainProcess {
    private static final Logger logger = LoggerFactory.getLogger(MainProcess.class);
    private static final Object lock = new Object();
    public static void main(String[] args) throws IOException, InterruptedException, DocumentException, BadHanyuPinyinOutputFormatCombination {
        String appName = "艺龙酒店";
        ApkDownloader apkDownloader = new ApkDownloader();
        String pkgName = apkDownloader.downloadApk(appName);
        if(pkgName.equals("")){
            logger.error("下载" + pkgName + "失败");
            System.exit(-1);
        }else{
            AppiumClient client = new AppiumClient(pkgName, lock);
            Thread thread1 = new Thread(client);
            thread1.start();
        }

//        appium.AppiumClient client = new appium.AppiumClient();
//        client.run("com.tencent.news_6.2.30_6230.apk");
        TShark tShark = new TShark(appName, pkgName.split("_")[0], lock);
        Thread thread2 = new Thread(tShark);
        thread2.start();

    }
}
