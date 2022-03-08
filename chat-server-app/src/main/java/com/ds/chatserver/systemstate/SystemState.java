package com.ds.chatserver.systemstate;

import java.util.HashMap;

public class SystemState {
    private static HashMap<String, ClientLog> clientLists;
    private static HashMap<String, ChatroomLog> chatroomLists;
    private static HashMap<String, ClientLog> draftClientLists;
    private static HashMap<String, ChatroomLog> draftChatroomLists;

    public HashMap<String, ClientLog> getClientLists() {
        return clientLists;
    }

    public static void setClientLists(HashMap<String, ClientLog> clientLists) {
        SystemState.clientLists = clientLists;
    }

    public static HashMap<String, ChatroomLog> getChatroomLists() {
        return chatroomLists;
    }

    public static void setChatroomLists(HashMap<String, ChatroomLog> chatroomLists) {
        SystemState.chatroomLists = chatroomLists;
    }

    public static void addClient(ClientLog clientLog) {

    }

    public static void removeClient(ClientLog clientLog) {

    }

    public static void addChatroom(ChatroomLog chatroomLog) {

    }

    public static void removeChatroom(ChatroomLog chatroomLog) {

    }

    public static Boolean isClientCommitted(String clientId) {
        return clientLists.containsKey(clientId);
    }

    public static Boolean isClientAvailableInDraft(String clientId) {
        return draftClientLists.containsKey(clientId);
    }

    public static void getChatroom(String chatroomId) {

    }
}
