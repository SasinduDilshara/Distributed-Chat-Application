package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

@Slf4j
public class LeaderState extends ServerState {
    private HashMap<String, Integer> nextIndex;
    private HashMap<String, Integer> matchIndex;

    public LeaderState(Server server) {
        super(server);
        log.info("Leader State : {}", this.server.getCurrentTerm());
        nextIndex = new HashMap<String, Integer>();
        matchIndex = new HashMap<String, Integer>();
        this.initState();
    }

    public void initState() {
        this.server.setLeaderId(this.server.getServerId());
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        Set<String> serverIds = ServerConfigurations.getServerIds();

        for(String id: serverIds){
            nextIndex.put(id, this.server.getLastLogIndex());
            matchIndex.put(id, 0);
        }

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }
            //TODO: complete
            JSONObject request = ServerServerMessage.requestAppendEntries(
                    this.server.getCurrentTerm(),
                    this.server.getLeaderId(),
                    0,
                    0,
                    null,
                    0
                    );
            try {
                // TODO: handle response
                log.info("Sending AppendEntries to {} in term {}", id, this.server.getCurrentTerm());
                Thread thread = new Thread(new ServerRequestSender( id, request,queue));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        for (int i = 0; i < serverCount-1; i++) {
            try {
                JSONObject response =  queue.take();
                if((Boolean) response.get("error")) {
                    log.info("Append Entries False");
                } else {
                    log.info("Append Entries Success: {}", response.get("success"));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void heartBeatAndLeaderElect() throws IOException {
        while(true){
            log.info("Leader State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void changeState(Server server) {}


    @Override
    public JSONObject handleRequestVote(JSONObject request) {
        if (this.server.getCurrentTerm() < Integer.parseInt((String)request.get(TERM))) {
            this.server.setState(new FollowerState(this.server));
            return this.server.getState().handleRequestVote(request);
        }
        return ServerServerMessage.responseVote(this.server.getCurrentTerm(), false);
    }
}
