package com.ds.chatserver.log;

public enum EventType {
    NEW_IDENTITY,
    CREATE_ROOM,
    ROOM_CHANGE,
    DELETE_ROOM,
    QUIT,
    //TODO: Cannot store in room change
    SERVER_CHANGE
}
