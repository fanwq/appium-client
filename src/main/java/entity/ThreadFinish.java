package entity;

public class ThreadFinish {
    private boolean isFinish;

    public ThreadFinish(boolean isFinish){
        this.isFinish = isFinish;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }
}
