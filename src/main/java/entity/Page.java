package entity;

import java.util.List;

public class Page {
    private String packageName;
    private String activityName;
    private List<NodeInfo> nodeInfos;

    public Page(String packageName, String activityName, List<NodeInfo> nodeInfos) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.nodeInfos = nodeInfos;
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

    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }
}
