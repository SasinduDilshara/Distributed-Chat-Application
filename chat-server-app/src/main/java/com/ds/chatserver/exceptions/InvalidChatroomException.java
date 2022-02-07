package com.ds.chatserver.exceptions;

public class InvalidChatroomException extends Exception {
    public InvalidChatroomException(String message) {
        super(message);
    }

    public static String generateChatroomExistsMessage(String chatroomName) {
        return "Chatroom " + chatroomName + " already exists!";
    }
}
