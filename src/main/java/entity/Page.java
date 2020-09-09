package entity;

import java.util.List;

public class Page {
    private String packageName;
    private String activityName;
    private NodeInfo preNodeInfo;

    public Page(String packageName, String activityName, NodeInfo preNodeInfo) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.preNodeInfo = preNodeInfo;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public NodeInfo getPreNodeInfo() {
        return preNodeInfo;
    }

    public void setPreNodeInfo(NodeInfo preNodeInfo) {
        this.preNodeInfo = preNodeInfo;
    }
}
