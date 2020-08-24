package entity;

public class NodeInfo {
    private String text;
    private String clickable;
    private String bounds;

    public NodeInfo(String text, String clickable, String bounds) {
        this.text = text;
        this.clickable = clickable;
        this.bounds = bounds;
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

    @Override
    public String toString() {
        return "NodeInfo{" +
                "text='" + text + '\'' +
                ", clickable='" + clickable + '\'' +
                ", bounds='" + bounds + '\'' +
                '}';
    }
}
