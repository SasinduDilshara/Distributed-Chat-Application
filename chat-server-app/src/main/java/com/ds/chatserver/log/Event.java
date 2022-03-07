package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
    private EventType type;
    private int logIndex;
    private int logTerm;
}
