package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoomHandler;

//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
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
//        serverSocket.setSoTimeout(500);
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
                System.out.println("Waiting for new Client Connection... ");
                Thread thread = new Thread(new ClientThread(serverSocket.accept(), chatRoomHandler));
                thread.start();
                System.out.println("Found New Connection");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        // Destroy all the resources
    }



}
