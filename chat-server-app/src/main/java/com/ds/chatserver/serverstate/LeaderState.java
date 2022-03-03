package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
public class LeaderState extends ServerState {

    public LeaderState(Server server) {
        super(server);
        log.info("Leader State : {}", this.server.getCurrentTerm());
        this.initState();
    }

    public void initState() {
        this.server.setLeaderId(this.server.getServerId());
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        Set<String> serverIds = ServerConfigurations.getServerIds();

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
                Thread thread = new Thread(new ServerRequestSender( this.server.getServerId(), request,queue));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
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
    }

    @Override
    public void heartBeatAndLeaderElect() throws IOException {

    }

    @Override
    public void changeState(Server server) {}


    @Override
    public JSONObject handleRequestVote(JSONObject request) {
        if (this.server.getCurrentTerm() < Integer.parseInt((String)request.get("term"))) {
            this.server.setState(new FollowerState(this.server));
            return this.server.getState().handleRequestVote(request);
        }
        return ServerServerMessage.responseVote(this.server.getCurrentTerm(), false);
    }
}
