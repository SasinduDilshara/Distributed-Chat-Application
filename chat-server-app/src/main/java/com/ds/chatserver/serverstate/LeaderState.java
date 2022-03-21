package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerDetails;
import com.ds.chatserver.serverhandler.heartbeatcomponent.HeartBeatSenderThread;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.ServerServerMessage;
import com.ds.chatserver.utils.Util;
import com.ds.chatserver.utils.Validation;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

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
    public synchronized JSONObject handleCreateClientServerRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        Boolean success = false;
        if ((!(SystemState.isClientExist(clientId)) && Validation.validateClientID(clientId))) {
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
        if(success){
            log.info("Leader: New identity created: {}", clientId);
        }
        else{
            log.info("Leader: New identity creation Failed: {}", clientId);
        }
        return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleDeleteClientServerRequest(JSONObject request) {
        log.debug("Delete client started in leader. request:{}", request);
        String clientId = request.get(CLIENT_ID).toString();
        Boolean success = false;
        if (SystemState.isClientExist(clientId)) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.QUIT)
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
        if(success){
            log.info("Leader: Delete identity success: {}", clientId);
        } else{
            log.info("Leader: Delete identity failed: {}", clientId);
        }
        return ServerServerMessage.getDeleteClientResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleCreateChatroomServerRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String roomId = request.get(ROOM_ID).toString();
        String former = SystemState.getCurrentChatroomOfClient(clientId);
        Boolean success = false;
        if (!(SystemState.isChatroomExist(roomId)) && Validation.validateRoomID(roomId, server.getServerId())
                && !SystemState.isOwner(clientId)) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.CREATE_ROOM)
                    .parameter(roomId)
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
        if(success){
            log.info("Leader: Create room success: {}", roomId);
        } else{
            log.info("Leader: Create room failed: {}", roomId);
        }
        return ServerServerMessage.getCreateChatroomResponse(server.getCurrentTerm(), former, success);
    }

    @Override
    public synchronized JSONObject handleDeleteChatroomServerRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String roomId = request.get(ROOM_ID).toString();
        Boolean success = false;
        if (SystemState.isChatroomExist(roomId) && SystemState.checkOwnerFromChatroom(roomId, clientId)) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.DELETE_ROOM)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .parameter(roomId)
                    .build());
            int lastLogIndexToCommit = this.server.getRaftLog().getLastLogIndex();
            success = replicateLogs();
            if (success) {
                this.server.getRaftLog().setCommitIndex(Math.max(lastLogIndexToCommit,
                        this.server.getRaftLog().getCommitIndex()));
                SystemState.commit(this.server);
            }
        }
        if(success){
            log.info("Leader: Delete room success: {}", roomId);
        } else{
            log.info("Leader: Delete room failed: {}", roomId);
        }
        return ServerServerMessage.getDeleteRoomResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleChangeRoomServerRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String senderServerId = request.get(SENDER_ID).toString();
        String formerRoomId = request.get(FORMER).toString();
        String newRoomId = request.get(ROOM_ID).toString();
        boolean success = false;
        String newServerId = "";

        if (SystemState.isClientExist(clientId) && SystemState.isChatroomExist(formerRoomId)
                && SystemState.isChatroomExist(newRoomId) && !SystemState.isOwner(clientId)
                && SystemState.isMemberOfChatroom(clientId, formerRoomId)) {
            /** client exists
            former chatroom exists
            new chatroom exists
            client not the owner of a chatroom
            client is a member of the prev room
             */
            newServerId = SystemState.getChatroomFromName(newRoomId).getServerId();
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(senderServerId)
                    .type(EventType.JOIN_ROOM)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .parameter(newRoomId)
                    .build());
            int lastLogIndexToCommit = this.server.getRaftLog().getLastLogIndex();
            success = replicateLogs();
            if (success) {
                this.server.getRaftLog().setCommitIndex(Math.max(lastLogIndexToCommit,
                        this.server.getRaftLog().getCommitIndex()));
                SystemState.commit(this.server);
            }
        }
        if(success){
            log.info("Leader: Changeroom success from: {} to: {}", formerRoomId, newRoomId);
        } else{
            log.info("Leader: Changeroom failed from: {} to: {}", formerRoomId, newRoomId);
        }
        return ServerServerMessage.getChangeRoomResponse(server.getCurrentTerm(), success, newServerId);
    }

    @Override
    public synchronized JSONObject handleMoveJoinServerRequest(JSONObject request) {
        String clientId = (String) request.get(CLIENT_ID);
        String roomId = (String) request.get(ROOM_ID);
        String senderId = (String) request.get(SENDER_ID);
        Boolean success = false;

        if (SystemState.isClientExist(clientId)) {
            if(!(SystemState.isChatroomExist(roomId) && senderId.equals(SystemState.getChatroomServer(roomId)))) {
                roomId = Util.getMainhall(senderId);
            }
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(senderId)
                    .type(EventType.ROUTE)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .parameter(roomId)
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
        if(success){
            log.info("Leader: Movejoin success to: {}", roomId);
        } else{
            log.info("Leader: Movejoin failed to: {}", roomId);
        }
        return ServerServerMessage.getMoveJoinResponse(server.getCurrentTerm(), roomId, success);
    }

    @Override
    public synchronized JSONObject handleServerInitServerRequest(JSONObject request){
        String serverId = (String) request.get(SERVER_ID);
        Boolean success = false;

        server.getRaftLog().insert(Event.builder()
                .serverId(serverId)
                .type(EventType.SERVER_INIT)
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

        if(success){
            log.info("Leader: Server Init success serverId: {}", serverId);
        } else{
            log.info("Leader: Server Init failed serverId: {}", serverId);
        }
        return ServerServerMessage.getServerInitResponse(server.getCurrentTerm(), success);
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
        JSONObject response = handleCreateClientServerRequest(ServerServerMessage.getCreateClientRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                this.server.getServerId()
        ));

        return ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS));
    }

    @Override
    protected JSONObject respondToDeleteRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject response = handleDeleteChatroomServerRequest(ServerServerMessage.getDeleteRoomRequest(
                this.server.getCurrentTerm(),
                clientId,
                this.server.getServerId(),
                roomId
        ));
        jsonObjects.add(ServerMessage.getDeleteRoomResponse(
                roomId,
                (Boolean) response.get(SUCCESS)
        ));
        if ((Boolean) response.get(SUCCESS)) {
            jsonObjects.add(ServerMessage.getRoomChangeResponse(
                    clientId, roomId, Util.getMainhall(this.server.getServerId())));
        }
        return ServerMessage.getJsonResponses(jsonObjects);
    }

    @Override
    protected JSONObject respondToJoinRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String former = (String) request.get(FORMER);
        String roomId = (String) request.get(ROOM_ID);
        JSONObject response = handleChangeRoomServerRequest(ServerServerMessage.getChangeRoomRequest(
                this.server.getCurrentTerm(),
                clientId,
                former,
                roomId,
                this.server.getServerId()
        ));
        Boolean success = (Boolean) response.get(SUCCESS);
        String newServerId = response.get(SERVER_ID).toString();
        if (success && !newServerId.equals(this.server.getServerId())) {
            ServerDetails sd = ServerConfigurations.getServerDetails(newServerId);
            String host = sd.getIpAddress();
            String port = String.valueOf(sd.getClientPort());
            return ServerMessage.getRouteResponse(roomId, host, port);
        }
        return ServerMessage.getRoomChangeResponse(clientId, former, success? roomId: former);
    }

    @Override
    protected JSONObject respondToCreateRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject response = handleCreateChatroomServerRequest(ServerServerMessage.getCreateChatroomRequest(
                this.server.getCurrentTerm(),
                clientId,
                roomId,
                this.server.getServerId()
        ));
        jsonObjects.add(ServerMessage.getCreateRoomResponse(
                roomId,
                (Boolean) response.get(SUCCESS)
        ));
        if ((Boolean) response.get(SUCCESS)) {
            jsonObjects.add(ServerMessage.getRoomChangeResponse(
                    clientId,
                    (String) response.get(FORMER),
                    roomId));
        }
        return ServerMessage.getJsonResponses(jsonObjects);
    }

    @Override
    protected JSONObject respondToMoveJoin(JSONObject request) {
        JSONObject response = handleMoveJoinServerRequest(ServerServerMessage.getMoveJoinRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                (String) request.get(FORMER),
                (String) request.get(ROOM_ID),
                this.server.getServerId()));
        JSONObject clientResponse = ServerMessage.getServerChangeResponse(
                (Boolean) response.get(SUCCESS), this.server.getServerId());
        clientResponse.put(ROOM_ID, response.get(ROOM_ID));
        return clientResponse;
    }

    @Override
    protected JSONObject respondToQuit(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID);
        JSONObject response = handleDeleteClientServerRequest(ServerServerMessage.getDeleteClientRequest(
                this.server.getCurrentTerm(),
                clientId,
                this.server.getServerId()
        ));
        if ((Boolean) response.get(SUCCESS)) {
            return ServerMessage.getRoomChangeResponse(
                    clientId,
                    roomId,
                    "");
        }
        return null;
    }

    @Override
    public JSONObject serverInit(JSONObject request) {
        return handleServerInitServerRequest(request);
    }
}
