package entity;

public class ApkInfo {
    private final String appName;
    private final String pkgName;
    private final String versionName;
    private final String versionCode;
    private final String apkUrl;
    private final String apkMd5;

    public ApkInfo(String appName, String pkgName, String versionName, String versionCode, String apkUrl, String apkMd5) {
        this.appName = appName;
        this.pkgName = pkgName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.apkUrl = apkUrl;
        this.apkMd5 = apkMd5;
    }

    public String getAppName() {
        return appName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public String getApkMd5() {
        return apkMd5;
    }

    @Override
    public String toString() {
        return "entity.ApkInfo{" +
                "appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", apkUrl='" + apkUrl + '\'' +
                ", apkMd5='" + apkMd5 + '\'' +
                '}';
    }
}
