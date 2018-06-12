package cn.larry.consensus.raft;

import cn.larry.consensus.raft.data.Logs;
import cn.larry.consensus.raft.msg.AppendEntry;
import cn.larry.consensus.raft.state.Candidate;
import cn.larry.consensus.raft.state.Follower;
import cn.larry.consensus.raft.state.Leader;

import java.util.List;

public class ServerState {

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public interface ServerStatus {
        String LEADER = "leader";
        String FOLLOWER = "follower";
        String CANDIDATE = "candidate";
    }

    private Server server;

    protected long currentTerm;
    protected int voteFor;

    protected long commitIndex;
    protected long lastApplied;

    private Logs logs;

    private String currentState;

    private Leader leader;
    private Follower follower;
    private Candidate candidate;

    private List<ServerInfo> knowServers;

    public void applyLog(AppendEntry appendEntry) {
        for (int index = 1; index < appendEntry.getEntries().size(); index++) {
            logs.getLogEntries().set(index + (int) (appendEntry.getPreLogIndex() - logs.getLastLogindex()), appendEntry.getEntries().get(index));
        }
        commitIndex = Math.min(appendEntry.getLeaderCommit(), logs.getLastLogindex());
    }

    public boolean isLeader(){
        return currentState.equals(ServerStatus.LEADER);
    }

    public boolean isFollower(){
        return currentState.equals(ServerStatus.FOLLOWER);
    }

    public boolean isCandidate(){
        return currentState.equals(ServerStatus.CANDIDATE);
    }


    public void convertToFollower() {


    }

    public void convertToLeader() {

    }


    public void convertToCandidate() {
        if(isCandidate())
            return;
        if(isFollower()){  //从follower切换到candidate

        }

    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public void setVoteFor(int voteFor) {
        this.voteFor = voteFor;
    }


    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public void setFollower(Follower follower) {
        this.follower = follower;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public void setKnowServers(List<ServerInfo> knowServers) {
        this.knowServers = knowServers;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public int getVoteFor() {
        return voteFor;
    }


    public long getCommitIndex() {
        return commitIndex;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Leader getLeader() {
        return leader;
    }

    public Follower getFollower() {
        return follower;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public List<ServerInfo> getKnowServers() {
        return knowServers;
    }

    public Logs getLogs() {
        return logs;
    }

    public void setLogs(Logs logs) {
        this.logs = logs;
    }
}
