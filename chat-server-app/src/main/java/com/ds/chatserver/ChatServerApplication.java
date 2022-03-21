package com.ds.chatserver;

import com.ds.chatserver.clienthandler.ClientRequestHandler;
import com.ds.chatserver.config.Configuration;
import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.DebugStateLog;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.SUCCESS;

@Slf4j
public class ChatServerApplication {

    public static void main(String[] args) throws FileNotFoundException {
        String serverId = args[0];
        String configFilePath = args[1];

        log.debug("Server Id : {}", serverId);

        new Configuration();
        ServerConfigurations.loadServerDetails(configFilePath);

        Server server = new Server(serverId);
        server.init(serverId);

        Thread debugLogThread = new Thread(new DebugStateLog(server));
        debugLogThread.start();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        server.getState().heartBeatAndLeaderElect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread heartBeatThread = new Thread(runnable);
        heartBeatThread.start();

        JSONObject serverInitResponse = null;
        while (serverInitResponse == null ||
                (serverInitResponse != null && !((Boolean) serverInitResponse.get(SUCCESS)))) {
            serverInitResponse = server.getState()
                    .serverInit(ServerServerMessage.getServerInitRequest(server.getCurrentTerm(),
                            server.getServerId()));
        }

        log.info("Server init finished. serverid: {}", server.getServerId());

        try {
            ClientRequestHandler clientRequestHandler = new ClientRequestHandler(server);
            clientRequestHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
