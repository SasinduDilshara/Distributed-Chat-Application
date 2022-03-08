package com.ds.chatserver.log;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class RaftLog {
    private ArrayList<Event> logEntries;
    private int commitIndex = -1;
    private int lastApplied = -1;

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
        if(index >= logEntries.size()){
            return -1;
        }
        return logEntries.get(index).getLogTerm();
    }

    public void incrementLastApplied(){
        this.lastApplied++;
    }

    public ArrayList<Event> getLogEntriesFromIndex(int index) {
        return (ArrayList<Event>) logEntries.subList(index, logEntries.size());
    }

    public void insert(Event event) {
        logEntries.add(event);
    }

    public int getIndexFromLastEntry() {
        //TODO current commit Index????
        return logEntries.get(logEntries.size() - 1).getLogIndex();
    }

    public int appendLogEntries(ArrayList<Event> entries) {
        ArrayList<Event> newEntries = new ArrayList<>();
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
        return entries.get(entries.size() - 1).getLogIndex();
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
        Event logEntry;
        int logSize = logEntries.size();
        if (logSize > logIndex) {
            //TODO: Implement with logIndex = Array Position
            for (int i = logSize - 1; i >= 0; i--) {
                logEntry = logEntries.get(i);
                if (logEntry.getLogIndex() == logIndex && logEntry.getLogTerm() == logTerm) {
                    return new int[]{LogEntryStatus.FOUND, i};
                } else if (logEntry.getLogIndex() == logIndex && logEntry.getLogTerm() != logTerm) {
                    return new int[]{LogEntryStatus.CONFLICT, i};
                }
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

    public int incrementLogIndex() {
        return getLastLogIndex() + 1;
    }
}
