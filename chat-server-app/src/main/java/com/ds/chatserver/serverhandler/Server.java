package com.ds.chatserver.serverhandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.clienthandler.ClientThread;

import java.util.ArrayList;

public class Server implements Runnable {
    ArrayList<ClientThread> clients;
    ArrayList<ChatRoom> chatrooms;
    int currentLogicalTimestamp;

    /*
     We need to imply
      - Leader detection - to create user and chatroom
        => createUser() -> leader.createUser() -> leader check and tell -> result will send to all nodes
        => If leader fails -> new leader gather the userlist using logical time

      - Time Synchronization (Use increment logical clock to add and remove)
      - Fault Tolerance
    */
    @Override
    public void run() {
        // Fault tolerance
        // If leader failed -> leaderAppoinment()
    }

    public void leaderAppoinment() {

    }

    public void faultTolerance() {

    }

    public static Boolean validateChatroom(String name) {
        //TODO Implement
        return true;
    }
}
