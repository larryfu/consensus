package cn.larry.consensus.raft;

import cn.larry.consensus.raft.msg.AppendEntry;
import cn.larry.consensus.raft.msg.AppendEntryRsp;
import cn.larry.consensus.raft.msg.Message;
import cn.larry.consensus.raft.msg.RequestVote;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class Server {

    private static final Logger  logger = LogManager.getLogger();



    private ServerState serverState;


    private LinkedBlockingQueue<Message> messageQueue;

    public void putMessage(Message message) {
        messageQueue.offer(message);
    }


    public void run() {
        while (true) {
            try {
                Message message = messageQueue.take();
                if (serverState.isLeader()) {
                    leaderHandleMessage(message);
                } else if (serverState.isFollower()) {
                    followerHandleMessage(message);
                } else if (serverState.isCandidate()) {
                    candidateHandleMessage(message);
                } else {

                }


            } catch (Exception e) {

            }

        }

    }

    private void candidateHandleMessage(Message message) {

    }

    private void followerHandleMessage(Message message) {
        if (message instanceof AppendEntry) {
            serverState.getFollower().onAppendEntry((AppendEntry) message);
        } else if (message instanceof RequestVote) {
            serverState.getFollower().onRequestVote((RequestVote) message);
        }else {
            logger.error("follower can not handle message ignore , "+message.getClass().getCanonicalName());
        }

    }

    private void leaderHandleMessage(Message message) {

    }
}
