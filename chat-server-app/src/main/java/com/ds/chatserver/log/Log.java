package com.ds.chatserver.log;

import java.util.ArrayList;

public class Log {
    private ArrayList<Event> logEntries;
    private int commitIndex;
    private int lastApplied;

    public ArrayList<Event> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(ArrayList<Event> logEntries) {
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

    public void insert(Event eventType) {

    }

    public void overwrite(Event eventType) {

    }

    public void delete() {

    }

    public void update(Event eventType) {

    }

    public int getIndexFromLastEntry() {
        //TODO current commit Index????
        return logEntries.get(logEntries.size() - 1).getLogIndex();
    }

    public int appendLogEntries(ArrayList<Event> entries) {
        //TODO Check this logic. Check the Event with the index and term of the prevLogIndex and prevLogTerm. Then go ahed from that index and check the entry is there
        ArrayList<Event> newEntries = new ArrayList<>();
        int[] checkResult;
        for (Event event: entries) {
            checkResult = checkLogIndexWithTerm(event.getLogIndex(), event.getTerm());
            if (checkResult[0] == LogEntryStatus.NOT_FOUND) {
                newEntries.add(event);
            } else if (checkResult[0] == LogEntryStatus.CONFLICT) {
                deleteEntriesFromIndex(checkResult[1]);
                newEntries.add(event);
            }
        }
        return entries.get(entries.size() - 1).getLogIndex();
    }

    public int deleteEntriesFromIndex(int logIndex) {
        for (int i = logEntries.size() - 1; i >=0 ; i--) {
            if (i < logIndex) {
                return i;
            }
            logEntries.remove(i);
        }
        return logIndex;
    }

    public int[] checkLogIndexWithTerm(int logIndex, int logTerm) {
        Event logEntry;
        int logSize = logEntries.size();
        //TODO Check this implementation
        if (logSize > logIndex) {
            for (int i = logSize - 1; i >= 0; i--) {
                logEntry = logEntries.get(i);
                if (logEntry.getLogIndex() == logIndex && logEntry.getTerm() == logTerm) {
                    return new int[]{LogEntryStatus.FOUND, i};
                } else if (logEntry.getLogIndex() == logIndex && logEntry.getTerm() != logTerm) {
                    return new int[]{LogEntryStatus.CONFLICT, i};
                }
            }
        }
        return new int[]{LogEntryStatus.NOT_FOUND, 0};
    }
}
