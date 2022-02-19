package com.ds.chatserver;

import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.clienthandler.ClientRequestHandler;
import com.ds.chatserver.config.ServerConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileNotFoundException;
import java.io.IOException;


public class ChatServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public static void main(String[] args) throws FileNotFoundException {
        logger.info("Application Started");
        ServerConfigurations.loadServerDetails("conf/conf.txt");
        logger.info("Configuration file loaded");
        ChatRoomHandler chatRoomHandler = ChatRoomHandler.getInstance();
        try {
            ClientRequestHandler clientRequestHandler = new ClientRequestHandler(chatRoomHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
