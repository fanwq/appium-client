package appium;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import entity.ApkInfo;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DownloadUtil;
import utils.MD5Util;

import java.io.*;
import java.util.*;

public class ApkDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ApkInfo.class);
    public String downloadApk(String appName) throws IOException {
        String url = "https://sj.qq.com/myapp/searchAjax.htm?kw=" + appName + "&pns=&sid=";
        HttpEntity entity = DownloadUtil.getDownloader().getUrl(url);
        String responseStr = EntityUtils.toString(entity);
        ApkInfo downloadUrl =  getDownloadUrl(responseStr, appName);
        String res = "fail";
        if(downloadUrl != null) {
            res = downLoadFile(downloadUrl, 0);
        }
        return res;
    }

    private ApkInfo getDownloadUrl(String jsonString, String searchName) throws IOException {
        JSONArray appList = ((JSONObject) JSON.parse(jsonString)).getJSONObject("obj").getJSONArray("items");
        List<ApkInfo> apkInfos = new ArrayList<>();
        ApkInfo info = null;
        StringBuilder tmpStr = new StringBuilder();
        for (Object o : appList) {
            String appName = ((JSONObject)o).getJSONObject("appDetail").getString("appName");
            String pkgName = ((JSONObject)o).getJSONObject("appDetail").getString("pkgName");
            String versionName = ((JSONObject)o).getJSONObject("appDetail").getString("versionName");
            String versionCode = ((JSONObject)o).getJSONObject("appDetail").getString("versionCode");
            String apkUrl = ((JSONObject)o).getJSONObject("appDetail").getString("apkUrl");
            String apkMd5 = ((JSONObject)o).getJSONObject("appDetail").getString("apkMd5");
            apkInfos.add(new ApkInfo(appName, pkgName, versionName, versionCode, apkUrl, apkMd5));
        }
        for(int i = 0; i < apkInfos.size(); i++){
            info = apkInfos.get(i);
            if(searchName.equals(info.getAppName().split("-")[0].trim())){
                return info;
            }
            tmpStr.append(i + 1).append(".").append(info.getAppName()).append("\n");
        }
        System.out.println("没有完全匹配的app名字，请从下面列表中选择一个，若没有请输入0：");
        System.out.println(tmpStr.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int num = Integer.parseInt(br.readLine());
        if(num == 0) {
            return null;
        }
        return apkInfos.get(num - 1);
    }

    private String downLoadFile(ApkInfo downloadUrl, int resetTime) throws IOException {
        if(resetTime++ > 3){
            return "";
        }
        String fileName = downloadUrl.getPkgName() + "_" + downloadUrl.getVersionName() + "_" + downloadUrl.getVersionCode() + ".apk";
        String filePre = "D:\\appium-apk\\";
        File apkFile = new File(filePre + fileName);
        if(apkFile.exists()){
            return fileName;
        }
        boolean is_create = apkFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(apkFile);
        String apkUrl = downloadUrl.getApkUrl();
        String md5 = downloadUrl.getApkMd5().toLowerCase();
        HttpEntity entity = DownloadUtil.getDownloader().getUrl(apkUrl);
        InputStream inputStream = entity.getContent();
        int length = 0;
        byte[] bytes = new byte[1024];
        logger.info("正在下载" + apkFile.getName());
        while ((length = inputStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, length);
        }
        fileOutputStream.close();
        inputStream.close();

        logger.info("下载完成，进行md5校验");
        String md5_cal = MD5Util.getMD5Three(apkFile).toLowerCase();
        if(!md5.equals(md5_cal)){
            logger.info("md5校验失败");
            logger.info("重试第" + resetTime + "次");
            return downLoadFile(downloadUrl, resetTime);
        }else {
            logger.info("md5校验成功！");
            return fileName;
        }
    }
}
