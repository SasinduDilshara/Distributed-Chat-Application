package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoomHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;

public class ClientRequestHandler {

    private Integer socketPort;
    private ServerSocket serverSocket;
    private ChatRoomHandler chatRoomHandler;


    public ClientRequestHandler (ChatRoomHandler chatRoomHandler) throws IOException {
        this.chatRoomHandler = chatRoomHandler;
        this.socketPort = 6666;
        serverSocket = new ServerSocket(socketPort);
    }

    public void setSocketPort(Integer socketPort) {
        this.socketPort = socketPort;
    }

    public ServerSocket getSocket() {
        return this.serverSocket;
    }

    @PostConstruct
    public void start() {
        while(true) {
            try {
                new ClientThread(serverSocket.accept(), chatRoomHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        // Destroy all the resources
    }



}
