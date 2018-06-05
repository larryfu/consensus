package cn.larry.consensus.raft.state;

import cn.larry.consensus.raft.ServerState;
import cn.larry.consensus.raft.msg.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Follower {

    private ServerState serverState;

    private long lastLeaderMsg = 0;  //上一次和Leader通信时间

    private int timeout; //Leader超时时间

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();



    public Follower(ServerState serverState,int timeout) {
        this.lastLeaderMsg = System.currentTimeMillis();
        this.serverState = serverState;
        this.timeout = timeout;
    }


    /**
     * follower处理AppendEntry消息
     *
     * @param appendEntry
     * @return
     */
    private AppednEntryRsp onAppendEntry(AppendEntry appendEntry) {
        this.lastLeaderMsg = System.currentTimeMillis();
        if (serverState.getCurrentTerm() > appendEntry.getTerm()) {
            return new AppednEntryRsp(serverState.getCurrentTerm(), false);
        }
        if (serverState.getCurrentTerm() < appendEntry.getTerm()) {
            serverState.setCurrentTerm(appendEntry.getTerm());
        }
        if (serverState.getLogs().getLastLogindex() < appendEntry.getPreLogIndex()) {
            return new AppednEntryRsp(serverState.getCurrentTerm(), false);
        } else {
            LogEntry logEntry = serverState.getLogs().getLogTerm(appendEntry.getPreLogIndex());
            if (logEntry == null) {

            }
            if (logEntry.getTerm() != appendEntry.getPreLogTerm()) {
                return new AppednEntryRsp(serverState.getCurrentTerm(), false);
            }
            serverState.applyLog(appendEntry);
            return new AppednEntryRsp(serverState.getCurrentTerm(), true);
        }
    }

    /**
     * Follower处理RequestVote消息
     *
     * @param requestVote
     * @return
     */
    private RequestVoteRsp onRequestVote(RequestVote requestVote) {
        if (serverState.getCurrentTerm() > requestVote.getTerm()) {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false);
        }
        if (serverState.getCurrentTerm() < requestVote.getTerm()) {
            serverState.setCurrentTerm(requestVote.getTerm());
        }
        LogEntry lastEntry = serverState.getLogs().getLastEntry();
        long lastIndex = lastEntry != null ? lastEntry.getIndex() : serverState.getLogs().getStartIndex();
        long lastterm = lastEntry != null ? lastEntry.getTerm() : serverState.getLogs().getStartTerm();
        if (requestVote.getLastLogTerm() >= lastterm && requestVote.getLastLogIndex() >= lastIndex) {
            return new RequestVoteRsp(serverState.getCurrentTerm(), true);
        } else {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false);
        }
    }


}
