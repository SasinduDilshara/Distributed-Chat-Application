package com.ds.chatserver;

import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.clienthandler.ClientRequestHandler;
import com.ds.chatserver.config.Configuration;
import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerIncomingRequestHandler;
import com.ds.chatserver.serverhandler.ServerIncomingRequestListener;
import com.ds.chatserver.serverhandler.ServerSenderHandler;
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

        while(true) {
            try {
                server.getState().heartBeatAndLeaderElect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        try {
//            Thread serverIncomingRequestListenerThread = new Thread(new ServerIncomingRequestListener(ServerConfigurations.getServerDetails(serverId).getServerPort()));
//            serverIncomingRequestListenerThread.start();
//            ServerSenderHandler serverSenderHandler = new ServerSenderHandler();
//            logger.info(serverId);
////            try {
////                if (serverId.equals("s1")){
////                    Thread.sleep(5000);
////                }
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("sender", serverId);
//            jsonObject.put("type", "test");
//            jsonObject.put("message", "Hello World");
//            if (serverId.equals("s1")) {
//                for (int i = 0; i < 10; i++) {
//                    jsonObject.put("count",i);
//                    serverSenderHandler.sendRequest("s2", jsonObject);
//                    logger.info("S1 send message {}", i);
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        logger.info("Configuration file loaded");
//        ChatRoomHandler chatRoomHandler = ChatRoomHandler.getInstance();
//        try {
//            ClientRequestHandler clientRequestHandler = new ClientRequestHandler(chatRoomHandler);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
