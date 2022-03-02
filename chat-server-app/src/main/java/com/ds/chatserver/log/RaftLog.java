package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class RaftLog {
    private ArrayList<EventType> logEntries;
    private int commitIndex;
    private int lastApplied;

    public void insert(EventType eventType) {

    }

    public void overwrite(EventType eventType) {

    }

    public void delete() {

    }

    public void update(EventType eventType) {

    }

}
