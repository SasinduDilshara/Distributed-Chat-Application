package com.ds.chatserver.exceptions;

public class InvalidChatroomException extends Exception {
    public InvalidChatroomException(String chatroomName) {
        super("Invalid name " + chatroomName + " for chat room The name should be ................");
    }
}
