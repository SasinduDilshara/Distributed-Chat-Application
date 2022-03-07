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
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.ServerConfigurationConstants.HEART_BEAT_FREQUENCY;

@Slf4j
public class HeartBeatSenderThread extends Thread{

    private Server server;
    private String receiverId;
    private Timestamp lastHeartBeatTimestamp;
    private Boolean exit;

    public HeartBeatSenderThread(Server server, String receiverId) {
        log.info("HB thread stated {}", receiverId);
        this.server = server;
        this.receiverId = receiverId;
        lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis() - 2* HEART_BEAT_FREQUENCY);
        exit = false;
    }

    @Override
    public void run() {

        while(!exit){
            Timestamp expireTimestamp = new Timestamp(System.currentTimeMillis() - HEART_BEAT_FREQUENCY);
            ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(2);

            if(expireTimestamp.after(lastHeartBeatTimestamp)){
                this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

                JSONObject request = ServerServerMessage.requestAppendEntries(
                        this.server.getCurrentTerm(),
                        this.server.getLeaderId(),
                        0,
                        0,
                        null,
                        0
                );

                try {
                    // TODO: handle response
                    Thread thread = new Thread(new ServerRequestSender( receiverId, request, queue, 1));
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject response =  queue.take();
                    if((Boolean) response.get("error")) {
                        if(exit)break;
//                        log.info("Append Entries False");
                    } else {
                        int responseTerm = Integer.parseInt((String) response.get("term"));
                        if(responseTerm > this.server.getCurrentTerm()){
                            this.server.setCurrentTerm(responseTerm);
                            this.server.setState(new FollowerState(this.server, null));
//                            this.stopThread();
                        }
//                        TODO
//                        log.info("Append Entries Success: {}", response.get("success"));
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
}
