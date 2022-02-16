package com.ds.chatserver.systemstate;

import java.util.HashMap;

public class SystemState {
    private HashMap<String, ClientLog> clientLists;
    private HashMap<String, ChatroomLog> chatroomLists;

    public HashMap<String, ClientLog> getClientLists() {
        return clientLists;
    }

    public void setClientLists(HashMap<String, ClientLog> clientLists) {
        this.clientLists = clientLists;
    }

    public HashMap<String, ChatroomLog> getChatroomLists() {
        return chatroomLists;
    }

    public void setChatroomLists(HashMap<String, ChatroomLog> chatroomLists) {
        this.chatroomLists = chatroomLists;
    }

    public void addClient(ClientLog clientLog) {

    }

    public void removeClient(ClientLog clientLog) {

    }

    public void addChatroom(ChatroomLog chatroomLog) {

    }

    public void removeChatroom(ChatroomLog chatroomLog) {

    }

    public void isClientAvailable(String clientId) {

    }

    public void getChatroom(String chatroomId) {

    }
}
