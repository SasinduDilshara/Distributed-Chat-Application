package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;

@Setter
@Getter
@AllArgsConstructor
@Slf4j
public abstract class ServerState {
    protected Server server;

    void changeState(Server server) {}

    public JSONObject respondToServerRequest(JSONObject request) {
        switch ((String) request.get("type")) {
            case "requestVote":
                return handleRequestVote(request);

            case "appendEntries":
                return handleRequestAppendEntries(request);
        }

        return null;
    }

    public abstract void initState();

    public abstract void heartBeatAndLeaderElect() throws IOException;

    public abstract JSONObject handleRequestVote(JSONObject jsonObject);

    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {

        int requestTerm = Integer.parseInt((String)jsonObject.get("term"));
        String leaderId = (String) jsonObject.get("leaderId");
        log.info("Append Entry {} from {}", requestTerm, leaderId);
        boolean success = false;
        if (requestTerm > this.server.getCurrentTerm()) {
            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setLeaderId(leaderId);
            log.info("Leader updated to {}", leaderId);
            this.server.setCurrentTerm(requestTerm);
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
