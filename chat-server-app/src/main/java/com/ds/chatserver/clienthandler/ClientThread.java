package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;

import java.net.Socket;

public class ClientThread implements Runnable {
    private String id;
    private Socket socket;
    private ChatRoom currentChatRoom;
    private ChatRoomHandler chatRoomHandler;
    private Boolean exit;

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler) {
        this.socket = socket;
        this.chatRoomHandler = chatRoomHandler;
        this.currentChatRoom = chatRoomHandler.getMainChatRoom();
        this.exit = true;
    }

    public void validate() {
        // Get approval from leader
    }

    public void sendMessage() {
        //Class the chatroom send message method
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        while(!exit) {

        }
    }

    public void parse(String message) {

    }


}
