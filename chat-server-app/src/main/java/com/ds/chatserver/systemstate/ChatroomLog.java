package com.ds.chatserver.systemstate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatroomLog {
    private String chatRoomName;
    private String ownerId;
    private String serverId;
}
