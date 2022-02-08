package com.ds.chatserver.exceptions;

public class ChatroomDoesntExistsException extends Exception {
    public ChatroomDoesntExistsException(String chatroomName) {
        super("Chatroom " + chatroomName + " doesn't exists!");
    }
}

