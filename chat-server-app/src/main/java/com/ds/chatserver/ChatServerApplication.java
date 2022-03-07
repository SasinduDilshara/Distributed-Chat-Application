package com.ds.chatserver;

import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.clienthandler.ClientRequestHandler;
import com.ds.chatserver.config.Configuration;
import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerIncomingRequestHandler;
import com.ds.chatserver.serverhandler.ServerIncomingRequestListener;
import com.ds.chatserver.serverhandler.ServerSenderHandler;
import com.ds.chatserver.utils.DebugStateLog;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileNotFoundException;
import java.io.IOException;


public class ChatServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);
    public static String myServerId;

    public static void main(String[] args) throws FileNotFoundException {
        String serverId = args[0];
        myServerId = serverId;
        String configFilePath = args[1];

        logger.debug("Server Id : {}", serverId);
        logger.debug("Configuration file path : {}", configFilePath);

        new Configuration();
        ServerConfigurations.loadServerDetails(configFilePath);

        Server server = new Server(serverId);
        server.init(serverId);

        Thread debugLogThread = new Thread(new DebugStateLog(server));
        debugLogThread.start();

        while(true) {
            try {
                server.getState().heartBeatAndLeaderElect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
