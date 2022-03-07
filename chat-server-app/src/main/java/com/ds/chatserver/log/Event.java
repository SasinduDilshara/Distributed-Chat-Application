package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
    private String clientId;
    private String serverId;
    private EventType type;
    private int logIndex;
    private int logTerm;
    private String parameter;
}
