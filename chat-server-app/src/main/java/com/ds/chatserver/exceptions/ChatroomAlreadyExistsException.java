package com.ds.chatserver.exceptions;

public class ChatroomAlreadyExistsException extends Exception {
    public ChatroomAlreadyExistsException(String chatroomName) {
        super("Chatroom " + chatroomName + " already exists!");
    }
}
