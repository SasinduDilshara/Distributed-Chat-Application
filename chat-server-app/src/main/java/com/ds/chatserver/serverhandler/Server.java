package com.ds.chatserver.serverhandler;

public class Server implements Runnable {
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

    }
}
