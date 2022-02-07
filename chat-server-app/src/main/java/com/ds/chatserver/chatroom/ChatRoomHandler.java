package com.ds.chatserver.chatroom;

import java.util.ArrayList;

public class ChatRoomHandler {
    private ArrayList<ChatRoom> chatrooms;
    private static ChatRoomHandler chatRoomHandler = getInstance();

    private static ChatRoomHandler getInstance() {
        return new ChatRoomHandler();
    }

    public void getChatroomfromClientId(String clientId) {

    }

    public ArrayList<ChatRoom> getChatrooms() {
        return chatrooms;
    }

    public void setChatrooms(ArrayList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }

    public static ChatRoomHandler getChatRoomHandler() {
        return chatRoomHandler;
    }
}
