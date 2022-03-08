package com.ds.chatserver.systemstate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ClientLog {
    private String clientId;
    private String chatroomName;
    private String serverId;
}
