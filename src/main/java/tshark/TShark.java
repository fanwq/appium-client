package tshark;

import entity.ThreadFinish;
import entity.URLInfo;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.dom4j.DocumentException;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class TShark implements Runnable{
    private final String appName, pkgName;
    private final Object lock;
    private ThreadFinish threadFinish;

    public TShark(String appName, String pkgName, Object lock, ThreadFinish threadFinish) {
        this.appName = appName;
        this.pkgName = pkgName;
        this.lock = lock;
        this.threadFinish = threadFinish;
    }

    @Override
    public void run() {
        try{
            TSharkConfig config = new TSharkConfig(appName, pkgName);
            config.config();
            Date dNow = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
            String dateString = sdf.format(dNow);
            File oriFile = new File(appName+ dateString + ".txt");
            File filFile = new File(appName + dateString + "-filtered.txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(oriFile), StandardCharsets.UTF_8));
            BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filFile), StandardCharsets.UTF_8));
            BufferedReader br = null;
            List<String> excludeUrls = config.getExcludeUrls();
            List<String> rules = config.getRules();
            bw1.write(rules.toString() + "\n");
            Process p;
            synchronized(this.lock){
                String interfaceString = getInterface(config.getDefaultInterface());
                String command = "tshark -i " + interfaceString + " -Y \"http.request\" " +
                        "-T fields -e http.request.method -e http.user_agent -e http.request.full_uri -l";
                p = Runtime.getRuntime().exec(command);
                this.lock.notify();
            }
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader finalBr = br;
            //注册钩子函数
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    try {
                        bw.close();
                        bw1.close();
                        finalBr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            //抓包并进行过滤
            String line;
            List<URLInfo> urls = new ArrayList<>();
            int i = 1;
            while((line = br.readLine()) != null){
                String[] param = line.split("\t");
                String method = param[0];
                String ua = param[1].replace("\n", "");
                String decodeUa = URLDecoder.decode(ua, "utf8");
                String url = param[2].replace("\n", "");
                String decodeUrl = URLDecoder.decode(url, "utf8");
                if(url.startsWith("https://")){
                    url = url.substring(8, url.indexOf('/', 8));
                }
                URLInfo model = new URLInfo(ua, url);
                boolean isMatch = false;
                for(String excludeUrl: excludeUrls){
                    isMatch = Pattern.matches(excludeUrl, url);
                    if(isMatch) break;
                }
                if(isMatch) continue;
                boolean isFound = false;
                for(String rule:rules){
                    if(decodeUa.toLowerCase().contains(rule)) isFound = true;
                    if(decodeUrl.toLowerCase().contains(rule)) isFound = true;
                    boolean isExist = false;
                    if(isFound){
                        for(URLInfo info: urls){
                            if(info.equals(model)){isExist  = true; break;}
                        }
                        if(!isExist){
                            urls.add(model);
                            bw1.write(i + ".\n");
                            bw1.write(ua + "\n");
                            bw1.write(url + "\n");
                            if(!ua.toLowerCase().contains(rule) && !ua.equals(decodeUa))
                                bw1.write(decodeUa + "\n");
                            if(!url.toLowerCase().contains(rule) && !url.equals(decodeUrl))
                                bw1.write(decodeUrl);
                        }
                    }
                }
                bw.write(i + ".\n");
                bw.write(ua + "\n");
                bw.write(url + "\n");
//                System.out.println(decodeUa);
//                System.out.println(decodeUrl + "\n");
                if(threadFinish.isFinish())
                    break;
                i++;
            }
            System.out.println("tshark循环跳出");
            bw.close();
            bw1.close();
        } catch (IOException | BadHanyuPinyinOutputFormatCombination | DocumentException e) {
            e.printStackTrace();
        }


    }

    public String getInterface(String defaultInterface) throws IOException {
        List<String> interfaceList = new ArrayList<>();
        String command = "tshark -D";
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line;
        System.out.println("本机中所有接口，请选择需要抓取的接口编号：");
        while((line = br.readLine()) != null){
            System.out.println(line);
            interfaceList.add(line.split("\\.")[1].split("\\(")[0].trim());
        }
        int num;
        if(!defaultInterface.equals("")) {
            num = Integer.parseInt(defaultInterface);
        }else {
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            num = Integer.parseInt(br1.readLine());
            br1.close();
        }
        System.out.println("在" + num + "号接口上进行抓取");
        br.close();
        return interfaceList.get(num - 1);
    }
}
