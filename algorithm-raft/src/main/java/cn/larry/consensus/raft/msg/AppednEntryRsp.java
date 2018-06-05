package cn.larry.consensus.raft.msg;

public class AppednEntryRsp {
    long term;
    boolean success;

    public AppednEntryRsp(long term,boolean success){
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
