package cn.larry.consensus.raft.state;

import cn.larry.consensus.raft.ServerState;

import java.util.Map;

public class Leader  {

    private ServerState serverState;

    Map<Integer,Long> nextIndexMap;
    Map<Integer,Long> matchIndexMap;

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



}
