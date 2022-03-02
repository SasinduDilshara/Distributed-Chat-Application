package com.ds.chatserver.systemstate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatroomLog {
    private String chatRoomName;
    private String ownerId;
    private String serverId;
}
