package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventType {
    private EventTypeName type;
    private int logIndex;
    private int logTerm;
}
