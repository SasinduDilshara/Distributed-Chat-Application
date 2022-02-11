package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private String id;
    private Socket socket;
    private ChatRoom currentChatRoom;
//    private ChatRoomHandler chatRoomHandler;
//    private PrintWriter printWriter;
//    private BufferedReader bufferedReader;
    private ClientThread clientThread;
    private Boolean exit;

    public Client(Socket socket, ChatRoomHandler chatRoomHandler) throws IOException {
        this.socket = socket;
//        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
//        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        this.chatRoomHandler = chatRoomHandler;
//        this.currentChatRoom = chatRoomHandler.getMainChatRoom();
        this.exit = false;
    }

    public void start() {
        // validate client identity

        // if validated
        // thread
    }

    public void stop() {

    }

}
