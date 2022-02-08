package com.ds.chatserver.exceptions;

public class ClientNotOwnerException extends Exception {
    public ClientNotOwnerException(String message) {
        super(message);
    }

    public static String generateClientNotOwnerMessage(String clientId, String chatroomName) {
        return "Client " + clientId + "is not the owner of chatroom" + chatroomName;
    }
}
