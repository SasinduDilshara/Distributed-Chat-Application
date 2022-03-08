package com.ds.chatserver.systemstate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
@AllArgsConstructor
public class ChatroomLog {
    private String chatRoomName;
    private String ownerId;
    private String serverId;
    private HashSet<String> participants;

    public void addParticipant(String participant){
        this.participants.add(participant);
    }

    public void removeParticipant(String participant){
        this.participants.remove(participant);
    }
}

