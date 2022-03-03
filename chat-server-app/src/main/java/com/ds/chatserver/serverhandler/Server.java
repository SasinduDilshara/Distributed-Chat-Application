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
    private int currentTerm;
    private int lastLogIndex;
    private int lastLogTerm;
    private int lastVotedTerm = -1;
    private String leaderId = null;
    private String lastVotedServerId = null;
    private RaftLog raftLog;
    private ArrayList<Integer> nestIndexes;
    private ArrayList<Integer> matchIndexes;

    public Server(String serverId) {
        this.serverId = serverId;
    }

    public void incrementTerm() {
        this.currentTerm ++;
    }

    public Boolean init(String serverId) {
//        this.state = new FollowerState(this);

        Integer serverPort = ServerConfigurations.getServerDetails(serverId).getServerPort();
        try {
            Thread requestListener = new Thread(new ServerIncomingRequestListener(serverPort, this));
            requestListener.start();
        } catch (IOException e) {
            log.error("");
            e.printStackTrace();
        }

        this.state = new CandidateState(this);

        return true;
    }

    public JSONObject handleServerRequest(JSONObject jsonObject) {
        return this.getState().respondToServerRequest(jsonObject);
    }
}
