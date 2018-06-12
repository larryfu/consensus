package cn.larry.consensus.raft.msg;

public class AppendEntryRsp extends Message{
    long term;
    boolean success;

    public AppendEntryRsp(long term, boolean success){
        this.term = term;
        this.success = success;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
