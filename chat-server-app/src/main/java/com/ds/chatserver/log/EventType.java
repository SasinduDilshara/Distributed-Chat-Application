package com.ds.chatserver.log;

public enum EventType {
    NEW_IDENTITY,
    CREATE_ROOM,
    JOIN_ROOM,
    DELETE_ROOM,
    QUIT,
    //TODO: Cannot store in room change
    SERVER_CHANGE
}
