package entity;

import java.util.Objects;

public class NodeInfo {
    private String text;
    private String clickable;
    private String bounds;
    private String resourceId;

    public NodeInfo(String text, String clickable, String bounds, String resourceId) {
        this.text = text;
        this.clickable = clickable;
        this.bounds = bounds;
        this.resourceId = resourceId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClickable() {
        return clickable;
    }

    public void setClickable(String clickable) {
        this.clickable = clickable;
    }

    public String getBounds() {
        return bounds;
    }

    public void setBounds(String bounds) {
        this.bounds = bounds;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "text='" + text + '\'' +
                ", clickable='" + clickable + '\'' +
                ", bounds='" + bounds + '\'' +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(text, nodeInfo.text) &&
                Objects.equals(clickable, nodeInfo.clickable) &&
                Objects.equals(bounds, nodeInfo.bounds) &&
                Objects.equals(resourceId, nodeInfo.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, clickable, bounds, resourceId);
    }
}
