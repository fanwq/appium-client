import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainProcess {
    private static final Logger logger = LoggerFactory.getLogger(MainProcess.class);
    public static void main(String[] args) throws IOException, InterruptedException, DocumentException {
//        ApkDownloader apkDownloader = new ApkDownloader();
//        String pkgName = apkDownloader.downloadApk("学而思网校");
//        if(pkgName.equals("")){
//            logger.error("下载" + pkgName + "失败");
//            System.exit(-1);
//        }else{
//            AppiumClient client = new AppiumClient();
//            client.run(pkgName);
//        }

        AppiumClient client = new AppiumClient();
        client.run("com.xueersi.parentsmeeting_8.07.01_80701.apk");
    }
}
