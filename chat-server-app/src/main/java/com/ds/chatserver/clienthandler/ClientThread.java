package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoomHandler;

import java.net.Socket;

public class ClientThread implements Runnable {
    private String id;
    private Socket socket;

    public void validate() {
        // Get approval from leader
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void run() {

    }
}
