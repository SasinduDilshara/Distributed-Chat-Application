package com.ds.chatserver;

import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.clienthandler.ClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;


public class ChatServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public static void main(String[] args) {
        logger.info("Application Started");
        ChatRoomHandler chatRoomHandler = ChatRoomHandler.getInstance();
        try {
            ClientRequestHandler clientRequestHandler = new ClientRequestHandler(chatRoomHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
