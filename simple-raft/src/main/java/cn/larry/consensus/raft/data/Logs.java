package cn.larry.consensus.raft.data;


import cn.larry.consensus.raft.proto.CommProtocolProto.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class Logs {

    private long startIndex;
    private long startTerm;
    private List<LogEntry> logEntries = new ArrayList<>();


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
        LogEntry entry = getLastEntry();
        if(entry == null){
            return 0L;
        }
        return entry.getIndex();
    }

    public LogEntry getLogEntry(long index) {
        if (logEntries.size() == 0 || index > getLastLogindex() || index < startIndex) {
            return null;
        } else {
            for (int i = logEntries.size() - 1; i >= 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
                if (logEntries.get(i).getIndex() == index)
                    return logEntries.get(i);
            }
        }
        return null;
    }

    public List<LogEntry> getLogByRange(long startIndex, long endIndex) {

        if (logEntries.size() == 0 ||startIndex > getLastLogindex())
            return new ArrayList<>();
        int start = -1;
        for (int i = logEntries.size() - 1; i >= 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
            if (logEntries.get(i).getIndex() == startIndex) {
                start = i;
                break;
            }
        }
        List<LogEntry> entries = new ArrayList<>();
        if (start >= 0) {
            for (int i = start; i < logEntries.size(); i++) {
                entries.add(logEntries.get(i));
                if (logEntries.get(i).getIndex() == endIndex)
                    break;
            }
        }
        return logEntries;
    }

    public LogEntry getPreLogEntry(long index) {
        if (index > getLastLogindex() || index < startIndex) {
            return null;
        } else {
            for (int i = logEntries.size() - 1; i > 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
                if (logEntries.get(i).getIndex() == index) {
                    return logEntries.get(i - 1);
                }
            }
        }
        return null;
    }

    public LogEntry getLastEntry() {
        if (logEntries.size() == 0) return null;
        return logEntries.get(logEntries.size() - 1);
    }

    public void addEntry(LogEntry entry) {
        logEntries.add(entry);
    }

    public long getStartTerm() {
        return startTerm;
    }

    public void setStartTerm(long startTerm) {
        this.startTerm = startTerm;
    }
}
