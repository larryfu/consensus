package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.proto.CommProtocolProto.LogEntry;
import cn.larry.consensus.raft.util.RandomString;

import java.util.List;

public class AppendEntry extends Msg {
    long term;
    int leaderId;
    long preLogIndex;
    long preLogTerm;
    List<LogEntry> entries;
    long leaderCommit;
    private boolean allReplace = false; //全量替换，用于初始化日志

    public boolean isAllReplace() {
        return allReplace;
    }

    public AppendEntry(){
        this.msgId = RandomString.genMsgId();
    }

    public void setAllReplace(boolean allReplace) {
        this.allReplace = allReplace;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public long getPreLogIndex() {
        return preLogIndex;
    }

    public void setPreLogIndex(long preLogIndex) {
        this.preLogIndex = preLogIndex;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}
