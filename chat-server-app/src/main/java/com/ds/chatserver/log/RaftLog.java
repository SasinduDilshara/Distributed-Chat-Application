package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class RaftLog {
    private ArrayList<Event> logEntries;
    private int commitIndex = 0;

    public int getLastLogIndex() {
        if (!logEntries.isEmpty()) {
            return logEntries.get(logEntries.size()-1).getLogIndex();
        }
        //TODO: recheck the default value
        return 0;
    }

    public int getLastLogTerm() {
        if (!logEntries.isEmpty()) {
            return logEntries.get(logEntries.size()-1).getLogTerm();
        }
        //TODO: recheck the default value
        return 0;
    }

    public int getTermFromIndex(int index) {
        return logEntries.get(index).getLogTerm();
    }

    public ArrayList<Event> getLogEntriesFromIndex(int index) {
        return (ArrayList<Event>) logEntries.subList(index, logEntries.size());
    }

    public void insert(Event event) {

    }

    public void overwrite(Event event) {

    }

    public void delete() {

    }

    public void update(Event event) {

    }

}
