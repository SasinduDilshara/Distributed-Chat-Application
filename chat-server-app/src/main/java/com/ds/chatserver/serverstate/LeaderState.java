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
        return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleDeleteClientRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String roomId = request.get(ROOM_ID).toString();
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
        return ServerServerMessage.getCreateChatroomResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleCreateChatroomRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String roomId = request.get(ROOM_ID).toString();
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
        return ServerServerMessage.getCreateChatroomResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleDeleteChatroomRequest(JSONObject request) {
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
        return ServerServerMessage.getDeleteRoomResponse(server.getCurrentTerm(), success);
    }

    @Override
    public synchronized JSONObject handleChangeRoomRequest(JSONObject request) {
        String clientId = request.get(CLIENT_ID).toString();
        String senderServerId = request.get(SENDER_ID).toString();
        String formerRoomId = request.get(FORMER).toString();
        String newRoomId = request.get(ROOM_ID).toString();
        boolean success = false;
        String newServerId = "";
        if (SystemState.isClientExist(clientId) && SystemState.isChatroomExist(formerRoomId)
                && SystemState.isChatroomExist(newRoomId) && !SystemState.isOwner(clientId)) {
            // client exists
            // former chatroom exists
            // new chatroom exists
            // client not the owner of former chatroom
            newServerId = SystemState.getChatroomFromName(newRoomId).getServerId();
            EventType eventType = newServerId.equals(senderServerId)? EventType.JOIN_ROOM: EventType.ROUTE;
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(senderServerId)
                    .type(eventType)
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
        return ServerServerMessage.getChangeRoomResponse(server.getCurrentTerm(), success, newServerId);
    }

    @Override
    public synchronized JSONObject handleMoveJoinRequest(JSONObject request) {
        String clientId = (String) request.get(CLIENT_ID);
        Boolean success = false;
        if (!(SystemState.isClientExist(clientId))) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.ROUTE)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .parameter((String) request.get(FORMER))
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
        return ServerServerMessage.getMoveJoinResponse(server.getCurrentTerm(), success);
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
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject response = handleDeleteChatroomRequest(ServerServerMessage.getDeleteRoomRequest(
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
        JSONObject response = handleChangeRoomRequest(ServerServerMessage.getChangeRoomRequest(
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
        JSONObject response = handleCreateChatroomRequest(ServerServerMessage.getCreateChatroomRequest(
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
                    clientId, Util.getMainhall(this.server.getServerId()),
                    roomId));
        }
        return ServerMessage.getJsonResponses(jsonObjects);
    }

    @Override
    protected JSONObject respondToMoveJoin(JSONObject request) {
        JSONObject response = handleMoveJoinRequest(ServerServerMessage.getMoveJoinRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                (String) request.get(FORMER),
                (String) request.get(ROOM_ID),
                this.server.getServerId()));
        return ServerMessage.getServerChangeResponse((Boolean) response.get(SUCCESS), this.server.getServerId());
    }

    @Override
    protected JSONObject respondToQuit(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID);
        JSONObject response = handleDeleteClientRequest(ServerServerMessage.getDeleteClientRequest(
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
}
