package com.ds.chatserver.systemstate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientLog {
    private String clientId;
    private String chatroomName;
    private String serverId;
}
