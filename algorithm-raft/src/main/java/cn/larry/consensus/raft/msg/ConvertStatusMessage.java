package cn.larry.consensus.raft.msg;

public class ConvertStatusMessage  extends Message{

    private String newStatus;

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public ConvertStatusMessage(String newStatus){
        this.newStatus = newStatus;
        this.from = "localhost";
        this.to = "localhost";
    }
}
