package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.heartbeatcomponent.HeartBeatSenderThread;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.ServerServerMessage;
import com.ds.chatserver.utils.Validation;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

@Slf4j
public class LeaderState extends ServerState {
    private Hashtable<String, Integer> nextIndex;
    private Hashtable<String, Integer> matchIndex;
    private List<HeartBeatSenderThread> hbSenderThreads;

    public LeaderState(Server server) {
        super(server);
        log.info("Leader State : {}", this.server.getCurrentTerm());
        nextIndex = new Hashtable<String, Integer>();
        matchIndex = new Hashtable<String, Integer>();
        hbSenderThreads = new ArrayList<HeartBeatSenderThread>();

        this.initState();
    }

    public synchronized void initState() {
        this.server.setLeaderId(this.server.getServerId());
        int serverCount = ServerConfigurations.getNumberOfServers();
        Set<String> serverIds = ServerConfigurations.getServerIds();

        for(String id: serverIds){
            nextIndex.put(id, this.server.getRaftLog().getLastLogIndex()+1);
            matchIndex.put(id, -1);
        }

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }
            HeartBeatSenderThread tmpThread = new HeartBeatSenderThread(this.server, id, this.nextIndex, this.matchIndex);
            tmpThread.start();
            hbSenderThreads.add(tmpThread);
        }
        notifyAll();
    }

    @Override
    public void heartBeatAndLeaderElect() throws IOException {
//          TODO check communication failurs and setstate to follower

//        log.info("Leader State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.prinhandleCreateClientRequesttStackTrace();
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
        Boolean success = false;

        if (requestTerm > this.server.getCurrentTerm()) {
            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setState(new FollowerState(this.server, leaderId));

            return this.server.getState().handleRequestAppendEntries(jsonObject);
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
        Boolean success = false;
        if (!(SystemState.isClientExist(clientId))) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.NEW_IDENTITY)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .build());

            int lastLogIndexToCommit = this.server.getRaftLog().getLastLogIndex();
            success = replicateLogs();
            if (success) {
                //Commit client
                this.server.getRaftLog().setCommitIndex(Math.max(lastLogIndexToCommit,
                        this.server.getRaftLog().getCommitIndex()));
                SystemState.commit(this.server);
            }
        }
        return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleCreateChatroomRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String roomId = request.get(ROOM_ID).toString();
        Boolean success = false;
        if (SystemState.isChatroomExist(roomId) && Validation.validateRoomID(roomId)) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.CREATE_ROOM)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .build());
            int lastLogIndexToCommit = this.server.getRaftLog().getLastLogIndex();
            success = replicateLogs();
            if (success) {
                this.server.getRaftLog().setCommitIndex(Math.max(lastLogIndexToCommit,
                        this.server.getRaftLog().getCommitIndex()));
                SystemState.commit(this.server);
            }
        }
        return ServerServerMessage.getCreateChatroomResponse(server.getCurrentTerm(), success);
    }

    private boolean replicateLogs() {
        int serverCount = ServerConfigurations.getNumberOfServers();
        Set<String> serverIds = ServerConfigurations.getServerIds();
        int logIndexToMatch = this.server.getRaftLog().getLastLogIndex();
        Set<String> matchedNodes = new HashSet<>();
        matchedNodes.add(this.server.getServerId());

        for(HeartBeatSenderThread thread : hbSenderThreads){
            thread.invokeImmediateSend();
        }

        while(true){
            for (String id: serverIds) {
                if(this.matchIndex.get(id) >= logIndexToMatch){
                    matchedNodes.add(id);
                }
            }

            if(matchedNodes.size() > serverCount/2){
                return true;
            }

            if(!this.server.getServerId().equals(this.server.getLeaderId())){
                return false;
            }
        }
    }

    @Override
    public String printState(){
        return "Leader State - Term: " + this.server.getCurrentTerm()
                + " Leader: " + this.server.getLeaderId()
                + " LastLogIndex: " + this.server.getRaftLog().getLastLogIndex();
    }

    @Override
    public JSONObject respondToNewIdentity(JSONObject request){
//        log.debug(request.toString());
        JSONObject response = handleCreateClientRequest(ServerServerMessage.getCreateClientRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                this.server.getServerId()
        ));
//        log.debug(ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS)).toString());

        return ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS));
    }

    @Override
    protected JSONObject respondToDeleteRoom(JSONObject request) {
        return null;
    }

    @Override
    protected JSONObject respondToJoinRoom(JSONObject request) {
        return null;
    }

    @Override
    protected JSONObject respondToCreateRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID_2);
        JSONObject response = handleCreateChatroomRequest(ServerServerMessage.getCreateChatroomRequest(
                this.server.getCurrentTerm(),
                clientId,
                roomId,
                this.server.getServerId()
        ));
        if ((Boolean) response.get(SUCCESS)) {
            return ServerMessage.getRoomChangeResponse(
                    clientId, "",
                    roomId);
        }
        return null;
    }

    @Override
    protected JSONObject respondToMoveJoin(JSONObject request) {
        return null;
    }

    @Override
    protected JSONObject respondToQuit(JSONObject request) {
        return null;
    }
}
