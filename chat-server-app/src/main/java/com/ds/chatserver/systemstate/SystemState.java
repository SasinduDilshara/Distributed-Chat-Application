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

    public static void commitClient(ClientLog clientLog) {
        clientLists.put(clientLog.getClientId(), clientLog);
        draftChatroomLists.remove(clientLog.getClientId());
    }

    public static void addDraftClient(ClientLog clientLog) {
        draftClientLists.put(clientLog.getClientId(), clientLog);
    }

    public static void removeClient(ClientLog clientLog) {
        clientLists.remove(clientLog.getClientId());
    }

    public static void commitChatroom(ChatroomLog chatroomLog) {
        chatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
        draftChatroomLists.remove(chatroomLog.getChatRoomName());
    }

    public static void addDraftChatroom(ChatroomLog chatroomLog) {
        draftChatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
    }

    public static void removeChatroom(ChatroomLog chatroomLog) {
        chatroomLists.remove(chatroomLog.getChatRoomName());
    }

    public static Boolean isClientCommitted(String clientId) {
        return clientLists.containsKey(clientId);
    }

    public static Boolean isClientAvailableInDraft(String chatroomName) {
        return draftClientLists.containsKey(chatroomName);
    }

    public static Boolean isChatroomCommitted(String chatroomName) {
        return chatroomLists.containsKey(chatroomName);
    }

    public static Boolean isChatroomAvailableInDraft(String clientId) {
        return draftChatroomLists.containsKey(clientId);
    }


    public static void getChatroom(String chatroomId) {

    }
}
