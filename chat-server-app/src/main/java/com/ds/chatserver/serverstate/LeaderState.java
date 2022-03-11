package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.serverhandler.heartbeatcomponent.HeartBeatSenderThread;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.ServerServerMessage;
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
        log.info(request.toString());
        String clientId = request.get(CLIENT_ID).toString();
        Boolean success = false;
        if (!(SystemState.isClientCommitted(clientId))) {
            server.getRaftLog().insert(Event.builder()
                    .clientId(clientId)
                    .serverId(request.get(SENDER_ID).toString())
                    .type(EventType.NEW_IDENTITY)
                    .logIndex(server.getRaftLog().getNextLogIndex())
                    .logTerm(server.getCurrentTerm())
                    .build());

            success = replicateLogs();
            if (success) {
                //Commit client
                SystemState.commit(this.server);
            }
        }
        log.info(ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), success).toString());
        return ServerServerMessage.getCreateClientResponse(server.getCurrentTerm(), success);
    }
//
//    @Override
//    public synchronized JSONObject handleDeleteClientRequest(JSONObject request) {
//        String clientId = request.get(CLIENT_ID).toString();
//        Boolean success = false;
//        //TODO: Change if concurrently handle requests
//        if (SystemState.isClientCommitted(clientId)) {
//            server.getRaftLog().insert(Event.builder()
//                    .clientId(clientId)
//                    .serverId(request.get(SERVER_ID).toString())
//                    .type(EventType.QUIT)
//                    .logIndex(server.incrementLogIndex())
//                    .logTerm(server.getCurrentTerm())
//                    .build());
//            success = replicateLogs();
//            if (success) {
//                SystemState.removeClient(new ClientLog(clientId, request.get(CHATROOM_NAME).toString(),
//                        request.get(SERVER_ID).toString()));
//            }
//        }
//        return ServerServerMessage.getDeleteClientResponse(server.getCurrentTerm(), success);
//    }
//
//    public JSONObject handleCreateChatroomRequest(JSONObject request) {
//        String chatroomName = request.get(CHATROOM_NAME).toString();
//        Boolean success = false;
//        if (!(SystemState.isChatroomAvailableInDraft(chatroomName) || SystemState.isChatroomCommitted(chatroomName))) {
//            server.getRaftLog().insert(Event.builder()
//                    .clientId(request.get(CLIENT_ID).toString())
//                    .serverId(request.get(SERVER_ID).toString())
//                    .type(EventType.CREATE_ROOM)
//                    .logIndex(server.incrementLogIndex())
//                    .logTerm(server.getCurrentTerm())
//                    .build());
////            ChatroomLog chatroom = new ChatroomLog(chatroomName, request.get(CLIENT_ID).toString(),
////                    request.get(SERVER_ID).toString());
//            // Add chatroom to draft
////            SystemState.addDraftChatroom(chatroom);
//            success = replicateLogs();
//            if (success) {
//                //Commit chatroom
////                SystemState.commitChatroom(chatroom);
//            }
//        }
//        return ServerServerMessage.getCreateChatroomResponse(server.getCurrentTerm(), success);
//    }
//
//    @Override
//    public synchronized JSONObject handleDeleteChatroomRequest(JSONObject request) {
//        String chatroomName = request.get(CHATROOM_NAME).toString();
//        Boolean success = false;
//        //TODO: Change if concurrently handle requests
//        if (SystemState.isChatroomCommitted(chatroomName)) {
//            server.getRaftLog().insert(Event.builder()
//                    .clientId(request.get(CLIENT_ID).toString())
//                    .serverId(request.get(SERVER_ID).toString())
//                    .type(EventType.DELETE_ROOM)
//                    .logIndex(server.incrementLogIndex())
//                    .logTerm(server.getCurrentTerm())
//                    .build());
//            success = replicateLogs();
//            if (success) {
//                SystemState.removeChatroom(new ChatroomLog(chatroomName, request.get(CLIENT_ID).toString(),
//                        request.get(SERVER_ID).toString()));
//            }
//        }
//        return ServerServerMessage.getDeleteRoomResponse(server.getCurrentTerm(), success);
//    }

    private boolean replicateLogs() {
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        Set<String> serverIds = ServerConfigurations.getServerIds();
//        int successReponsesCount = 1;
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
//                log.info("Replication Succees");
                return true;
            }

            if(!this.server.getServerId().equals(this.server.getLeaderId())){
//                log.info("Replication Failed");
                return false;
            }
        }

//        Thread thread = null;

//        for (String id: serverIds) {
//            if (id.equals(this.server.getServerId())) {
//                continue;
//            }
//            try {
//                thread = new Thread(new ServerRequestSender( id, createAppendEntriesJSONMessage(id), queue, 1));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            thread.start();
//        }

//        while (true) {
//            if (!(server.getLeaderId().equals(server.getServerId()))) {
//                return false;
//            }
//            try {
//                JSONObject response = queue.take();
//
//                log.info(response.toString());
//                String responseServerId = (String) response.get(RECEIVER_ID);
//                if (((Boolean) response.get(ERROR)) || !((Boolean) response.get(SUCCESS))) {
//                    if(!(Boolean) response.get(ERROR)){
//                        nextIndex.put(responseServerId, nextIndex.get(responseServerId) - 1);
//                    }
//
//                    thread = new Thread(new ServerRequestSender(responseServerId,
//                            createAppendEntriesJSONMessage(responseServerId), queue, 1));
//                    thread.start();
//                    continue;
//                }
//                successReponsesCount += 1;
//                nextIndex.put(responseServerId, server.getRaftLog().getLastLogIndex());
//                //TODO: Check
//                matchIndex.put(responseServerId, server.getRaftLog().getLastLogIndex());
//                if (successReponsesCount > serverCount / 2) {
//                    server.getRaftLog().setCommitIndex(server.getRaftLog().getLogEntries().size() - 1);
//                    return true;
//                }
//            } catch (InterruptedException | IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    private JSONObject createAppendEntriesJSONMessage(String serverId) {
//        return ServerServerMessage.getAppendEntriesRequest(server.getCurrentTerm(),
//                server.getServerId(),
//                // previous log index get from next index
//                nextIndex.get(serverId),
//                server.getRaftLog().getTermFromIndex(nextIndex.get(serverId)),
//                server.getRaftLog().getLogEntriesFromIndex(nextIndex.get(serverId)),
//                server.getRaftLog().getCommitIndex());
//    }

    @Override
    public String printState(){
        return "Leader State - Term: " + this.server.getCurrentTerm() + " Leader: " + this.server.getLeaderId()
                + " LastLogIndex: " + this.server.getRaftLog().getLastLogIndex();
    }

    @Override
    public JSONObject respondNewIdentity(JSONObject request){
        log.info(request.toString());
        JSONObject response = handleCreateClientRequest(ServerServerMessage.getCreateClientRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                this.server.getServerId()
        ));
        log.info(ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS)).toString());

        return ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS));
    }
}
