package com.ds.chatserver.serverhandler.heartbeatcomponent;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.serverstate.CandidateState;
import com.ds.chatserver.serverstate.FollowerState;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.ServerConfigurationConstants.HEART_BEAT_FREQUENCY;

@Slf4j
public class HeartBeatSenderThread extends Thread{

    private Server server;
    private String receiverId;
    private Timestamp lastHeartBeatTimestamp;
    private Boolean exit;
    private Boolean immediateSend;
    private Hashtable<String, Integer> nextIndex;
    private Hashtable<String, Integer> matchIndex;

    public HeartBeatSenderThread(Server server, String receiverId,
                                 Hashtable<String, Integer> nextIndex, Hashtable<String, Integer> matchIndex) {
        this.server = server;
        this.receiverId = receiverId;
        lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis() - 2* HEART_BEAT_FREQUENCY);
        exit = false;
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.immediateSend = false;
    }

    @Override
    public void run() {

        while(!exit){
            Timestamp expireTimestamp = new Timestamp(System.currentTimeMillis() - HEART_BEAT_FREQUENCY);
            ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(2);

            if(this.immediateSend || expireTimestamp.after(lastHeartBeatTimestamp)){
                this.immediateSend = false;
                this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

                int previousLogIndex = nextIndex.get(receiverId)-1;
                int previousLogTerm = server.getRaftLog().getTermFromIndex(previousLogIndex);
                int lastLogIndex = server.getRaftLog().getLastLogIndex();

                JSONObject request = ServerServerMessage.getAppendEntriesRequest(
                        this.server.getCurrentTerm(),
                        this.server.getLeaderId(),
                        previousLogIndex,
                        previousLogTerm,
                        server.getRaftLog().getLogEntriesFromIndex(previousLogIndex),
                        server.getRaftLog().getCommitIndex()
                );

                try {
                    Thread thread = new Thread(new ServerRequestSender( receiverId, request, queue, 1));
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject response =  queue.take();
                    if((Boolean) response.get(ERROR)) {
                        if(exit)break;
                    } else {
                        int responseTerm = Integer.parseInt((String) response.get(TERM));
                        if(responseTerm > this.server.getCurrentTerm()){
                            this.server.setCurrentTerm(responseTerm);
                            this.server.setState(new FollowerState(this.server, null));
                            break;
                        }

                        if ((Boolean) response.get(SUCCESS)) {
                            nextIndex.put(receiverId, lastLogIndex+1);
                            matchIndex.put(receiverId, lastLogIndex);
                        } else {
                            nextIndex.put(receiverId, previousLogIndex - 1);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void stopThread(){
        exit = true;
    }

    public void invokeImmediateSend(){
        this.immediateSend = true;
    }
}
