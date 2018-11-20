package cn.larry.consensus.raft;


import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.proto.CommProtocolProto.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Leader {

    private RaftAlgorithm serverState;
    private  Logger logger = LogManager.getLogger("StateFlow");

    Map<Integer, Long> nextIndexMap = new ConcurrentHashMap<>();
    Map<Integer, Long> matchIndexMap = new ConcurrentHashMap<>();
    Map<Long, List<Runnable>> logCallBacks = new ConcurrentHashMap<>();
    Map<Integer, Long> heartbeatMap = new ConcurrentHashMap<>();

    ExecutorService executorService;

    ScheduledExecutorService scheduledExecutorService;

    private int heartbeat;

    public Leader(RaftAlgorithm serverState) {
        this.serverState = serverState;
    }


    public Map<Integer, Long> getNextIndexMap() {
        return nextIndexMap;
    }

    public void setNextIndexMap(Map<Integer, Long> nextIndexMap) {
        this.nextIndexMap = nextIndexMap;
    }

    public Map<Integer, Long> getMatchIndexMap() {
        return matchIndexMap;
    }

    public void setMatchIndexMap(Map<Integer, Long> matchIndexMap) {
        this.matchIndexMap = matchIndexMap;
    }


    public void init() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.serverState.incrCurrentTerm();
        this.serverState.getClusterServers();
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                keepHeatBeat();
            }
        }, heartbeat, TimeUnit.SECONDS);
    }

    /**
     * 发送心跳appendEntry
     */
    private void keepHeatBeat() {
        if (serverState.isLeader()) {
            for (ServerInfo serverInfo : serverState.getClusterServers()) {
                long lastHeartbeat = heartbeatMap.get(serverInfo.getServerId());
                if (System.currentTimeMillis() - lastHeartbeat >= heartbeat * 1000) {
                    sendAppendEntryToFollower(serverInfo);
                }

            }
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    keepHeatBeat();
                }
            }, heartbeat, TimeUnit.SECONDS);
        }
    }

    private void sendAppendEntryToFollower(ServerInfo serverInfo) {
        //本机,无需发送
        if (serverInfo.getServerId() == serverState.getThisServer().getServerId())
            return;
        //向follower发送log entry
        long matchIndex = matchIndexMap.get(serverInfo.getServerId());
        long nextIndex = nextIndexMap.get(serverInfo.getServerId());
        LogEntry preEntry = serverState.getLogs().getLogEntry(matchIndex);
        AppendEntry appendEntry = new AppendEntry();
        appendEntry.setPreLogTerm(preEntry.getTerm());
        appendEntry.setPreLogIndex(preEntry.getIndex());
        appendEntry.setTo(serverInfo.getServerName());
        appendEntry.setFrom(serverState.getThisServer().getServerName());
        appendEntry.setTerm(serverState.currentTerm);
        appendEntry.setLeaderId(serverState.getThisServer().getServerId());
        appendEntry.setLeaderCommit(serverState.getCommitIndex());
        List<LogEntry> entries = serverState.getLogs().getLogByRange(nextIndex, serverState.getLogs().getStartIndex());
        appendEntry.setEntries(entries);
        serverState.sendMessage(appendEntry);
    }

    /**
     * 处理客户端请求
     *
     * @param request
     * @throws InterruptedException
     */
    public void onClientRequest(ClientRequest request) {
        LogEntry.Builder entry  = LogEntry.newBuilder();
        entry.setTerm(serverState.currentTerm);
        entry.setIndex(serverState.getLogs().getLastLogindex() + 1);
        entry.setCommand(request.getCommand());
        serverState.getLogs().addEntry(entry.build());
        for (ServerInfo serverInfo : serverState.getClusterServers()) {
            sendAppendEntryToFollower(serverInfo);
        }
        logCallBacks.putIfAbsent(entry.getIndex(), new ArrayList<>());
        logCallBacks.get(entry.getIndex()).add(new Runnable() {
            @Override
            public void run() {
                Consumer runnable = serverState.getMsgCallBack().get(request.getMsgId());
                if (runnable != null) {
                    /**
                     * 客户端请求处理成功，回调回复客户端
                     */
                    runnable.accept(null);
                }
            }
        });
    }

    /**
     * leader处理appendEntry的回复，
     * 这里有一个问题，由于算法是单线程事件驱动模式异步处理消息的，收到回复时已经没有发消息时的上下文了，
     * 如果采用异步回调模式（类似于js）代码会变得非常复杂，这里先采用简单一点的方式在回复里附上请求
     *
     * @param rsp
     */
    public Msg onAppendEntryRsp(AppendEntryRsp rsp) {

        ServerInfo fromServer = null;

        for (ServerInfo server : serverState.getClusterServers()) {
            if (server.getServerName().equals(rsp.getFrom())) {
                fromServer = server;
            }
        }

        if (fromServer == null) { // 无法找到来源serve信息，忽略
            return null;
        }
        heartbeatMap.put(fromServer.getServerId(), System.currentTimeMillis());

        if (rsp.isSuccess()) { //appendEntry成功
            if (rsp.getRequest().getEntries().size() > 0) {  //非心跳，有复制log，更新Follower状态
                Long matchIndex = rsp.getRequest().getEntries().get(rsp.getRequest().getEntries().size() - 1).getIndex();
                matchIndexMap.put(fromServer.getServerId(), matchIndex);
                nextIndexMap.put(fromServer.getServerId(), matchIndex + 1);
                updateCommit();
            }
            return null;
        } else { //append entry 失败，回退prelogIndex然后重试
            AppendEntry appendEntry = new AppendEntry();
            appendEntry.setFrom(serverState.getThisServer().getServerName());
            appendEntry.setTo(fromServer.getServerName());
            appendEntry.setLeaderCommit(serverState.commitIndex);
            appendEntry.setLeaderId(serverState.getThisServer().getServerId());
            appendEntry.setTerm(serverState.currentTerm);
            LogEntry entry = serverState.getLogs().getLogEntry(rsp.getRequest().getPreLogIndex());
            LogEntry preEntry = serverState.getLogs().getPreLogEntry(rsp.getRequest().getPreLogIndex());
            if (preEntry == null || entry == null) {
                //回退到达起点,强制follower全量替换日志
                appendEntry.setAllReplace(true);
                appendEntry.setPreLogIndex(rsp.getRequest().getPreLogIndex());
                appendEntry.setPreLogTerm(rsp.getRequest().getPreLogTerm());
            } else {
                appendEntry.setPreLogIndex(preEntry.getIndex());
                appendEntry.setPreLogTerm(preEntry.getTerm());
            }
            appendEntry.setEntries(new ArrayList<>());
            if (entry != null) {
                appendEntry.getEntries().add(entry);
            }
            appendEntry.getEntries().addAll(rsp.getRequest().getEntries());
            return appendEntry;
        }
    }

    /**
     * 计算当前已复制到过半server的日志index
     */
    private void updateCommit() {
        List<Long> matchIndexs = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : matchIndexMap.entrySet()) {
            matchIndexs.add(entry.getValue());
        }
        Collections.sort(matchIndexs);
        //将已复制的log index排序后刚好在一半前的那个位置的log index是commitIndex 如果数组长度是奇数则是(n-1)/2，数组长度是偶数则是n/2-1
        long commitIndex = matchIndexs.get((int) (Math.ceil((double) matchIndexs.size() / 2) - 1));
        if (commitIndex > serverState.commitIndex) {
            long originIndex = serverState.getCommitIndex();
            serverState.commitIndex = commitIndex;
            for (long index = originIndex + 1; index <= serverState.commitIndex; index++) {
                logCommitted(index);
            }
        }
    }

    /**
     * 执行日志提交之后的回调，通知客户端执行成功
     *
     * @param index
     */
    private void logCommitted(long index) {
        List<Runnable> runnables = logCallBacks.get(index);
        if (runnables != null) {
            for (Runnable runnable : runnables)
                executorService.submit(runnable);
        }
        logCallBacks.remove(index);
    }


    public RequestVoteRsp onRequestVote(RequestVote requestVote) {

        if (requestVote.getTerm() <= serverState.currentTerm) {
            return new RequestVoteRsp(serverState.currentTerm, false,requestVote);
        } else {
            this.serverState.convertToFollower(requestVote.getTerm(), 0);
            return null;
        }

    }

    public AppendEntryRsp onAppendEntry(AppendEntry appendEntry) {
        if (appendEntry.getTerm() <= serverState.getCurrentTerm()) {
            return new AppendEntryRsp(serverState.currentTerm, false, appendEntry);
        } else {
            serverState.convertToFollower(appendEntry.getTerm(), appendEntry.getLeaderId());
            this.serverState.putMessage(appendEntry,null);
            return null;
        }
    }

}
