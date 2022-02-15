package com.ds.chatserver.exceptions;

public class ClientAlreadyInChatRoomException extends Exception {
    public ClientAlreadyInChatRoomException(String message) {
        super(message);
    }

    public static String generateClientAlreadyInChatRoomMessage(String clientId, String chatroomName) {
        return "Client " + clientId + "already exists in the chatroom" + chatroomName;
    }
}
