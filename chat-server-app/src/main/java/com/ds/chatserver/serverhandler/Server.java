package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.log.RaftLog;
import com.ds.chatserver.serverstate.CandidateState;
import com.ds.chatserver.serverstate.FollowerState;
import com.ds.chatserver.serverstate.ServerState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

@Setter
@Getter
@Slf4j
public class Server {
    private ServerState state;
    private String serverId;
    private int currentTerm = 0;
    private int lastLogIndex = 0;
    private int lastLogTerm = 0;
    private int lastVotedTerm = -1;
    private String leaderId = null;
    private String lastVotedServerId = null;
    private RaftLog raftLog;

    public Server(String serverId) {
        this.serverId = serverId;
    }

    public void incrementTerm() {
        this.currentTerm ++;
    }

    public Boolean init(String serverId) {
        this.state = new FollowerState(this, null);

        Integer serverPort = ServerConfigurations.getServerDetails(serverId).getServerPort();
        try {
            Thread requestListener = new Thread(new ServerIncomingRequestListener(serverPort, this));
            requestListener.start();
        } catch (IOException e) {
            log.error("");
            e.printStackTrace();
        }

        return true;
    }

    public JSONObject handleServerRequest(JSONObject jsonObject) {
        return this.getState().respondToServerRequest(jsonObject);
    }

    public void setState(ServerState state){
        this.state.stop();
        this.state = state;
    }

    public int incrementLogIndex() {
        setLastLogIndex(getLastLogIndex() + 1);
        return getLastLogIndex();
    }
}
