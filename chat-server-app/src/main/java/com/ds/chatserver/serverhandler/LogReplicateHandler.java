package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.utils.ServerServerMessage;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.ERROR;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.SUCCESS;
import static com.ds.chatserver.constants.ServerConfigurationConstants.SERVER_ID;

public class LogReplicateHandler extends Thread {
    private Server server;
    private Hashtable<String, Integer> nextIndex;
    private Hashtable<String, Integer> matchIndex;

    public LogReplicateHandler(Server server, Hashtable<String, Integer> nextIndex, Hashtable<String, Integer> matchIndex) {
        this.server = server;
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    private JSONObject createJSONMessage(String serverId) {
        return ServerServerMessage.getAppendEntriesRequest(server.getCurrentTerm(),
                server.getServerId(),
                // previous log index get from next index
                nextIndex.get(serverId),
                server.getRaftLog().getTermFromIndex(nextIndex.get(serverId)),
                server.getRaftLog().getLogEntriesFromIndex(nextIndex.get(serverId)),
                server.getRaftLog().getCommitIndex());
    }

    @Override
    public void run() {
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        Set<String> serverIds = ServerConfigurations.getServerIds();
        int successReponsesCount = 1;
        Thread thread = null;

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }
            try {
                thread = new Thread(new ServerRequestSender( id, createJSONMessage(id), queue, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.start();
        }

        while(successReponsesCount < serverCount) {
            try {
                JSONObject response = queue.take();
                if (!((Boolean) response.get(ERROR))) {
                    String responseServerId = (String) response.get(SERVER_ID);
                    if ((Boolean) response.get(SUCCESS)) {
                        successReponsesCount += 1;
                        nextIndex.put(responseServerId, server.getLastLogIndex());
                        //TODO: Check
                        matchIndex.put(responseServerId, server.getLastLogIndex());
                        if (successReponsesCount > serverCount/2) {
                            server.getRaftLog().setCommitIndex(server.getRaftLog().getCommitIndex() + 1);
                        }
                    } else {
                        nextIndex.put(responseServerId, nextIndex.get(responseServerId) - 1);
//                        thread = new Thread(new ServerRequestSender(responseServerId,
//                                createJSONMessage(responseServerId), queue, 1));
//                        thread.start();
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
