package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;

import java.util.ArrayList;

public class ChatRoom {
    private ArrayList<ClientThread> clients;
    private String owner;

    public ArrayList<ClientThread> getClients() {
        return clients;
    }

    public void setClients(ArrayList<ClientThread> clients) {
        this.clients = clients;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void send() {
        // send message
        // inform all clients
    }
}
