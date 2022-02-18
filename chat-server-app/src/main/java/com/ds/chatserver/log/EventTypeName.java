package com.ds.chatserver.log;

public enum EventTypeName {
    NEWIDENTITY,
    CREATEROOM,
    ROOMCHANGE,
    DELETEROOM,
    QUIT,
    //TODO: Cannot store in room change
    SERVERCHANGE
}