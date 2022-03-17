package com.ds.chatserver.systemstate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
public class ChatroomLog {
    private String chatRoomName;
    private String ownerId;
    private String serverId;
    private HashSet<String> participants;

    public ChatroomLog(String chatRoomName, String ownerId, String serverId) {
        this.chatRoomName = chatRoomName;
        this.ownerId = ownerId;
        this.serverId = serverId;
        this.participants = new HashSet<>();
    }

    public HashSet<String> getParticipants() {
        return (HashSet<String>) participants.clone();
    }

    public void addParticipant(String participant){
        this.participants.add(participant);
    }

    public void removeParticipant(String participant){
        this.participants.remove(participant);
    }
}

