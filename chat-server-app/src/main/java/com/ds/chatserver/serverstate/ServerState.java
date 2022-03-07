package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;

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
        }

        //TODO: return null cause and error
        return null;
    }

    public abstract void initState();

    public void stop(){

    }

    public abstract String printState();

    public abstract void heartBeatAndLeaderElect() throws IOException;

    public abstract JSONObject handleRequestVote(JSONObject request);

    public abstract JSONObject handleRequestAppendEntries(JSONObject request);

    public abstract JSONObject handleCreateClientRequest(JSONObject request);
}
