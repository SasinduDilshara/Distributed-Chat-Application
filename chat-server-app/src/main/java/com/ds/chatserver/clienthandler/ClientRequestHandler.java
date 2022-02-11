package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoomHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;


public class ClientRequestHandler {

    private Integer socketPort;
    private ServerSocket serverSocket;
    private ChatRoomHandler chatRoomHandler;
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public ClientRequestHandler (ChatRoomHandler chatRoomHandler) throws IOException {
        logger.info("Initialize the Client Request Handler...");
        this.chatRoomHandler = chatRoomHandler;
        this.socketPort = 6666;
        serverSocket = new ServerSocket(socketPort);
        this.start();
    }

    public void setSocketPort(Integer socketPort) {
        this.socketPort = socketPort;
    }

    public ServerSocket getSocket() {
        return this.serverSocket;
    }

    public void start() {
        while(true) {
            try {
                logger.info("Waiting for new Client Connection ... ");
                Thread thread = new Thread(new ClientThread(serverSocket.accept(), chatRoomHandler));
//                Thread thread = new Thread (
//                        ClientThread.builder()
//                                .socket(serverSocket.accept())
//                                .chatRoomHandler(chatRoomHandler)
//                                .build()
//                );
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        // Destroy all the resources
    }



}
