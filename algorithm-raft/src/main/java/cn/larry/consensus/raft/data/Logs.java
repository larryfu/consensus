package cn.larry.consensus.raft.data;

import cn.larry.consensus.raft.msg.LogEntry;

import java.util.List;

public class Logs {

    private long startIndex;
    private long startTerm;
    private List<LogEntry> logEntries;



    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public long getLastLogindex() {
        return getLastEntry().getIndex();
    }

    public LogEntry getLogTerm(long index){
        if(index> getLastLogindex() || index<startIndex){
            return null;
        }else {
           for(int i = logEntries.size()-1;i>=0;i--){ //向前遍历找到匹配的index，同一Term内index是连续的
               if(logEntries.get(i).getIndex() == index)
                   return logEntries.get(i);
           }
        }
        return null;
    }

    public LogEntry getLastEntry(){
        if(logEntries.size() == 0 )return null;
        return logEntries.get(logEntries.size()-1);
    }

    public long getStartTerm() {
        return startTerm;
    }

    public void setStartTerm(long startTerm) {
        this.startTerm = startTerm;
    }
}
