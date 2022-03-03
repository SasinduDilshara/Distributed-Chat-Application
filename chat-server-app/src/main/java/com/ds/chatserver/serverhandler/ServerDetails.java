package com.ds.chatserver.serverhandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ServerDetails {
    private String serverId;
    private int serverPort;
    private int clientPort;
    private String ipAddress;
}
