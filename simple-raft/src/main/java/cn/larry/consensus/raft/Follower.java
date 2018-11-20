package cn.larry.consensus.raft;

import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Follower {

    private  Logger logger = LogManager.getLogger("StateFlow");

    private RaftAlgorithm serverState;

    private long lastLeaderMsg = 0;  //上一次和Leader通信时间

    private int timeoutSeconds; //Leader超时时间

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    public void init() {
        this.lastLeaderMsg = System.currentTimeMillis();
        scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                checkLeaderTimeout();
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    public Follower(final RaftAlgorithm serverState, int timeoutSeconds) {
        this.lastLeaderMsg = System.currentTimeMillis();
        this.serverState = serverState;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 检查Leader超时
     */
    private void checkLeaderTimeout() {
        if (serverState.isFollower()) {
            if (System.currentTimeMillis() - lastLeaderMsg > timeoutSeconds * 1000) {
                serverState.putMessage(new ConvertStatusMsg(RaftAlgorithm.ServerStatus.CANDIDATE),null);
            } else {
                scheduledExecutorService.schedule(new Runnable() {
                    public void run() {
                        checkLeaderTimeout();
                    }
                }, timeoutSeconds, TimeUnit.SECONDS);
            }
        }
    }


    /**
     * follower处理AppendEntry消息
     *
     * @param appendEntry
     * @return
     */
    public AppendEntryRsp onAppendEntry(AppendEntry appendEntry) {
        if (serverState.getCurrentTerm() > appendEntry.getTerm()) {
            return new AppendEntryRsp(serverState.getCurrentTerm(), false,appendEntry);
        }
        this.lastLeaderMsg = System.currentTimeMillis();
        if (serverState.getCurrentTerm() < appendEntry.getTerm()) {
            serverState.setCurrentTerm(appendEntry.getTerm());
        }
        if (serverState.getLogs().getLastLogindex() < appendEntry.getPreLogIndex()) {
            return new AppendEntryRsp(serverState.getCurrentTerm(), false,appendEntry);
        } else {
            CommProtocolProto.LogEntry logEntry = serverState.getLogs().getLogEntry(appendEntry.getPreLogIndex());
            if (logEntry == null) {

            }
            if (logEntry.getTerm() != appendEntry.getPreLogTerm()) {
                return new AppendEntryRsp(serverState.getCurrentTerm(), false,appendEntry);
            }
            serverState.applyLog(appendEntry);
            return new AppendEntryRsp(serverState.getCurrentTerm(), true,appendEntry);
        }
    }

    /**
     * Follower处理RequestVote消息
     *
     * @param requestVote
     * @return
     */
    public RequestVoteRsp onRequestVote(RequestVote requestVote) {
        if (serverState.getCurrentTerm() > requestVote.getTerm()) {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false,requestVote);
        }
        if (serverState.getCurrentTerm() < requestVote.getTerm()) {
            serverState.setCurrentTerm(requestVote.getTerm());
        }
        CommProtocolProto.LogEntry lastEntry = serverState.getLogs().getLastEntry();
        long lastIndex = lastEntry != null ? lastEntry.getIndex() : serverState.getLogs().getStartIndex();
        long lastterm = lastEntry != null ? lastEntry.getTerm() : serverState.getLogs().getStartTerm();
        if (requestVote.getLastLogTerm() >= lastterm && requestVote.getLastLogIndex() >= lastIndex) {
            serverState.setVoteFor(requestVote.getCandidateId());
            return new RequestVoteRsp(serverState.getCurrentTerm(), true,requestVote);
        } else {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false,requestVote);
        }
    }


}
