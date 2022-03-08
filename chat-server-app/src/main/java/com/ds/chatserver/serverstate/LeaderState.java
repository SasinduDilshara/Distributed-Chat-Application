package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.LogReplicateHandler;
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
        return ServerServerMessage.getRequestVoteResponse(this.server.getCurrentTerm(), false);
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
        JSONObject response = ServerServerMessage.getAppendEntriesResponse(
                this.server.getCurrentTerm(),
                success
        );
        return response;
    }

    @Override
    public synchronized JSONObject handleCreateClientRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        if (!(SystemState.isClientAvailableInDraft(clientId) || SystemState.isClientCommitted(clientId))) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SERVER_ID).toString())
                    .type(EventType.NEW_IDENTITY)
                    .logIndex(server.incrementLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .build());
            return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), replicateLogs());
        }
        return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), false);
    }

    private boolean replicateLogs() {
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        Set<String> serverIds = ServerConfigurations.getServerIds();
        int successReponsesCount = 1;
        Thread thread = null;

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }
            try {
                thread = new Thread(new ServerRequestSender( id, createJSONMessage(id), queue, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.start();
        }

        while (true) {
            if (!(server.getLeaderId().equals(server.getServerId()))) {
                return false;
            }
            try {
                JSONObject response = queue.take();
                String responseServerId = (String) response.get(RECEIVER_ID);
                if (((Boolean) response.get(ERROR)) || !((Boolean) response.get(SUCCESS))) {
                    nextIndex.put(responseServerId, nextIndex.get(responseServerId) - 1);
                    thread = new Thread(new ServerRequestSender(responseServerId,
                            createJSONMessage(responseServerId), queue, 1));
                    thread.start();
                    continue;
                }
                successReponsesCount += 1;
                nextIndex.put(responseServerId, server.getLastLogIndex());
                //TODO: Check
                matchIndex.put(responseServerId, server.getLastLogIndex());
                if (successReponsesCount > serverCount / 2) {
                    server.getRaftLog().setCommitIndex(server.getRaftLog().getLogEntries().size() - 1);
                    return true;
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject createJSONMessage(String serverId) {
        return ServerServerMessage.getAppendEntriesRequest(server.getCurrentTerm(),
                server.getServerId(),
                // previous log index get from next index
                nextIndex.get(serverId),
                server.getRaftLog().getTermFromIndex(nextIndex.get(serverId)),
                server.getRaftLog().getLogEntriesFromIndex(nextIndex.get(serverId)),
                server.getRaftLog().getCommitIndex());
    }

    @Override
    public String printState(){
        return "Leader State - Term: " + this.server.getCurrentTerm() + " Leader: " + this.server.getLeaderId();
    }
}
