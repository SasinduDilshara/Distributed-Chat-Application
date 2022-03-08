package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.serverhandler.ServerSenderHandler;
import com.ds.chatserver.serverhandler.heartbeatcomponent.HeartBeatSenderThread;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.ServerConfigurationConstants.SERVER_ID;

@Slf4j
public class LeaderState extends ServerState {
    private Hashtable<String, Integer> nextIndex;
    private Hashtable<String, Integer> matchIndex;
    private Timestamp lastHearBeatTimeStamp;
    private List<HeartBeatSenderThread> hbSenderThreads;

    public LeaderState(Server server) {
        super(server);
        log.info("Leader State : {}", this.server.getCurrentTerm());
        nextIndex = new Hashtable<String, Integer>();
        matchIndex = new Hashtable<String, Integer>();
        hbSenderThreads = new ArrayList<HeartBeatSenderThread>();

        this.initState();
    }

    public void initState() {
        this.server.setLeaderId(this.server.getServerId());
        int serverCount = ServerConfigurations.getNumberOfServers();
        Set<String> serverIds = ServerConfigurations.getServerIds();

        for(String id: serverIds){
            nextIndex.put(id, this.server.getLastLogIndex());
            matchIndex.put(id, 0);
        }

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }

            HeartBeatSenderThread tmpThread = new HeartBeatSenderThread(this.server, id);
            tmpThread.start();
            hbSenderThreads.add(tmpThread);

        }

    }

    @Override
    public void heartBeatAndLeaderElect() throws IOException {
//          TODO check communication failurs and setstate to follower

//        log.info("Leader State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public JSONObject handleRequestVote(JSONObject request) {
        if (this.server.getCurrentTerm() < Integer.parseInt((String)request.get(TERM))) {
            this.server.setState(new FollowerState(this.server, null));
            return this.server.getState().handleRequestVote(request);
        }
        return ServerServerMessage.responseVote(this.server.getCurrentTerm(), false);
    }

    @Override
    public void stop(){
        for (HeartBeatSenderThread thread : hbSenderThreads) {
            thread.stopThread();
        }
    }

    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        String leaderId = (String) jsonObject.get(LEADER_ID);
        log.info("Append Entry {} from {}", requestTerm, leaderId);
        boolean success = false;
        if (requestTerm > this.server.getCurrentTerm()) {
            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setState(new FollowerState(this.server, leaderId));
            success = true;
        }
        JSONObject response = ServerServerMessage.responseAppendEntries(
                this.server.getCurrentTerm(),
                success
        );
        return response;
    }

    @Override
    public JSONObject handleCreateClientRequest(JSONObject request) throws IOException {
        /*
            Request contains client name, server id
         */
        String clientId = request.get(CLIENT_ID).toString();
        if (!(SystemState.isClientAvailableInDraft(clientId) || SystemState.isClientCommitted(clientId))) {
//            ArrayBlockingQueue<JSONObject> arrayBlockingQueue =  new ArrayBlockingQueue<>();
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SERVER_ID).toString())
                    .type(EventType.NEW_IDENTITY)
                    .logIndex(server.incrementLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .build());
            //TODO: Handle race conditions if any
            //TODO: Create the JSon object using next indexes and match indexes
//            ServerSenderHandler.broadCastMessage(server.getServerId(), , arrayBlockingQueue);
            replicateLogs();
        }
        return null;
    }

    private boolean replicateLogs() throws IOException {

    }

    @Override
    public String printState(){
        return "Leader State - Term: " + this.server.getCurrentTerm() + " Leader: " + this.server.getLeaderId();
    }
}
