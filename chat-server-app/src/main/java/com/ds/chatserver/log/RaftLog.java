package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class RaftLog {
    private List<Event> logEntries;
    private int commitIndex;
    private int lastApplied;

    public RaftLog() {
        this.commitIndex = -1;
        this.lastApplied = -1;
        this.logEntries = new ArrayList<>();
    }

    public int getLastLogIndex() {
        if (!logEntries.isEmpty()) {
            return logEntries.get(logEntries.size()-1).getLogIndex();
        }

        return -1;
    }

    public int getLastLogTerm() {
        if (!logEntries.isEmpty()) {
            return logEntries.get(logEntries.size()-1).getLogTerm();
        }

        return -1;
    }

    public int getTermFromIndex(int index) {
        if(index < 0 || index >= logEntries.size()){
            return -1;
        }
        return logEntries.get(index).getLogTerm();
    }

    public void incrementLastApplied(){
        this.lastApplied++;
    }

    public List<Event> getLogEntriesFromIndex(int index) {
        if(index < 0){
            return logEntries;
        }
        if(index > logEntries.size()){
            return new ArrayList<>();
        }
        return logEntries.subList(index, logEntries.size());
    }

    public void insert(Event event) {
        logEntries.add(event);
    }

    public int appendLogEntries(List<Event> entries) {
        List<Event> newEntries = new ArrayList<>();
        int[] checkResult;
        for (Event event: entries) {
            checkResult = checkLogIndexWithTerm(event.getLogIndex(), event.getLogTerm());
            if (checkResult[0] == LogEntryStatus.NOT_FOUND) {
                newEntries.add(event);
            } else if (checkResult[0] == LogEntryStatus.CONFLICT) {
                deleteEntriesFromIndex(checkResult[1]);
                newEntries.add(event);
            }
        }
        logEntries.addAll(newEntries);

        return getLastLogIndex();
    }

    public void deleteEntriesFromIndex(int logIndex) {
        for (int i = logEntries.size() - 1; i >=0 ; i--) {
            if (i < logIndex) {
                return;
            }
            logEntries.remove(i);
        }
    }

    public int[] checkLogIndexWithTerm(int logIndex, int logTerm) {
        if(logIndex < 0){
            return new int[]{LogEntryStatus.FOUND, logIndex};
        }

        Event logEntry;
        int logSize = logEntries.size();
        if (logSize > logIndex) {
            logEntry = logEntries.get(logIndex);
            if (logEntry.getLogIndex() == logIndex && logEntry.getLogTerm() == logTerm) {
                return new int[]{LogEntryStatus.FOUND, logIndex};
            } else if (logEntry.getLogIndex() == logIndex && logEntry.getLogTerm() != logTerm) {
                return new int[]{LogEntryStatus.CONFLICT, logIndex};
            }
            else{
                return new int[]{LogEntryStatus.NOT_FOUND, 0};
            }
        }
        return new int[]{LogEntryStatus.NOT_FOUND, 0};
    }

    public Event getIthEvent(int index){
        if(index >= this.logEntries.size()){
            return null;
        }
        return this.logEntries.get(index);
    }

    public int getNextLogIndex() {
        return (getLastLogIndex() + 1);
    }
}
