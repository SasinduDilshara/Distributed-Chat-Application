package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.LEADER_ID;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.TERM;
import static com.ds.chatserver.constants.RequestTypeConstants.APPEND_ENTRIES;
import static com.ds.chatserver.constants.RequestTypeConstants.REQUEST_VOTE;

@Setter
@Getter
@AllArgsConstructor
@Slf4j
public abstract class ServerState {
    protected Server server;

    void changeState(Server server) {}

    public JSONObject respondToServerRequest(JSONObject request) {
        switch ((String) request.get(TERM)) {
            case REQUEST_VOTE:
                return handleRequestVote(request);

            case APPEND_ENTRIES:
                return handleRequestAppendEntries(request);
        }

        return null;
    }

    public abstract void initState();

    public abstract void heartBeatAndLeaderElect() throws IOException;

    public abstract JSONObject handleRequestVote(JSONObject jsonObject);

    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        String leaderId = (String) jsonObject.get(LEADER_ID);
        log.info("Append Entry {} from {}", requestTerm, leaderId);
        boolean success = false;
        if (requestTerm >= this.server.getCurrentTerm()) {
            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setLeaderId(leaderId);
            this.server.setState(new FollowerState(this.server));
            success = true;
        }
        JSONObject response = ServerServerMessage.responseAppendEntries(
                this.server.getCurrentTerm(),
                success
        );
        return response;
    }
}
