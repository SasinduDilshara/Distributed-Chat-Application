package com.ds.chatserver.log;

import java.util.ArrayList;

public class Log {
    private ArrayList<EventType> logEntries;
    private int commitIndex;
    private int lastApplied;

    public ArrayList<EventType> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(ArrayList<EventType> logEntries) {
        this.logEntries = logEntries;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public int getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(int lastApplied) {
        this.lastApplied = lastApplied;
    }

    public void insert(EventType eventType) {

    }

    public void overwrite(EventType eventType) {

    }

    public void delete() {

    }

    public void update(EventType eventType) {

    }

}
