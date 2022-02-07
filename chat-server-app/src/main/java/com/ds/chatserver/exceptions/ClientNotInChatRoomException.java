package com.ds.chatserver.exceptions;

public class ClientNotInChatRoomException extends Exception {
    public ClientNotInChatRoomException(String message) {
        super(message);
    }

    public static String generateClientNotInChatRoomMessage(String clientId, String chatroomName) {
        return "Client " + clientId + "is not in the chatroom" + chatroomName;
    }
}
