package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.*;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.RequestTypeConstants.*;

@Setter
@Getter
@AllArgsConstructor
@Slf4j
public abstract class ServerState {
    protected Server server;

    public JSONObject respondToServerRequest(JSONObject request) {
        switch ((String) request.get(TYPE)) {
            case REQUEST_VOTE:
                return handleRequestVote(request);
            case APPEND_ENTRIES:
                return handleRequestAppendEntries(request);
            case CREATE_CLIENT:
                return handleCreateClientRequest(request);
            case DELETE_CLIENT:
                return handleDeleteClientRequest(request);
            case CREATE_CHAT_ROOM:
                return handleCreateChatroomRequest(request);
            case DELETE_CHAT_ROOM:
                return handleDeleteChatroomRequest(request);
            case CHANGE_ROOM:
                return handleChangeRoomRequest(request);
            case SERVER_INIT:
                return handleServerInitRequest(request);
        }

        //TODO: return null cause and error
        return null;
    }

    public JSONObject respondToClientRequest(JSONObject request) {
//        log.debug("Client Req: {}", request.toString());
        switch ((String) request.get(TYPE)) {
            case NEW_IDENTITY:
                return respondToNewIdentity(request);
            case MOVE_JOIN:
                return respondToMoveJoin(request);
            case CREATE_ROOM:
                return respondToCreateRoom(request);
            case JOIN_ROOM:
                return respondToJoinRoom(request);
            case DELETE_ROOM:
                return respondToDeleteRoom(request);
            case QUIT:
                return respondToQuit(request);
        }
        return null;
    }

    public abstract void initState();

    public void stop(){

    }

    public abstract String printState();

    public abstract void heartBeatAndLeaderElect() throws IOException;

    public abstract JSONObject handleRequestVote(JSONObject request);

    public abstract JSONObject handleRequestAppendEntries(JSONObject request);

    public JSONObject handleCreateClientRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleDeleteClientRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleCreateChatroomRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleDeleteChatroomRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleChangeRoomRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleServerInitRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public abstract JSONObject respondToNewIdentity(JSONObject request);

    protected abstract JSONObject respondToDeleteRoom(JSONObject request);

    protected abstract JSONObject respondToJoinRoom(JSONObject request);

    protected abstract JSONObject respondToCreateRoom(JSONObject request);

    protected abstract JSONObject respondToMoveJoin(JSONObject request);

    protected abstract JSONObject respondToQuit(JSONObject request);
}
