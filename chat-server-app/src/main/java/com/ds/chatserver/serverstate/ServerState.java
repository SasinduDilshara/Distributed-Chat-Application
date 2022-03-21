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
                return handleCreateClientServerRequest(request);
            case DELETE_CLIENT:
                return handleDeleteClientServerRequest(request);
            case CREATE_CHAT_ROOM:
                return handleCreateChatroomServerRequest(request);
            case DELETE_CHAT_ROOM:
                return handleDeleteChatroomServerRequest(request);
            case CHANGE_ROOM:
                return handleChangeRoomServerRequest(request);
            case SERVER_INIT:
                return handleServerInitServerRequest(request);
            case MOVE_JOIN:
                return handleMoveJoinServerRequest(request);
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

    public JSONObject handleCreateClientServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleDeleteClientServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleCreateChatroomServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleDeleteChatroomServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleChangeRoomServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleServerInitServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public JSONObject handleMoveJoinServerRequest(JSONObject request) {
        //TODO: Return Error
        return null;
    }

    public abstract JSONObject respondToNewIdentity(JSONObject request);

    protected abstract JSONObject respondToDeleteRoom(JSONObject request);

    protected abstract JSONObject respondToJoinRoom(JSONObject request);

    protected abstract JSONObject respondToCreateRoom(JSONObject request);

    protected abstract JSONObject respondToMoveJoin(JSONObject request);

    protected abstract JSONObject respondToQuit(JSONObject request);

    public abstract JSONObject serverInit(JSONObject request);
}
