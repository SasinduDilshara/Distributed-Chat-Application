package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.Log;
import com.ds.chatserver.log.LogEntryStatus;
import com.ds.chatserver.serverresponse.AppendEntriesResult;
import com.ds.chatserver.serverresponse.RequestVoteResult;
import com.ds.chatserver.statehandler.ServerState;

import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {
    private ServerState state;
    private String serverId;
    private int currentTerm;
    private String votedFor = null;
    private Log logs;
    private ArrayList<Integer> nestIndexes;
    private ArrayList<Integer> matchIndexes;
//    private HashMap<String, ServerDetails> serverDetails = ServerConfigurations.loadServerDetails();

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public Log getLogs() {
        return logs;
    }

    public void setLogs(Log logs) {
        this.logs = logs;
    }

    public ArrayList<Integer> getNestIndexes() {
        return nestIndexes;
    }

    public void setNestIndexes(ArrayList<Integer> nestIndexes) {
        this.nestIndexes = nestIndexes;
    }

    public ArrayList<Integer> getMatchIndexes() {
        return matchIndexes;
    }

    public void setMatchIndexes(ArrayList<Integer> matchIndexes) {
        this.matchIndexes = matchIndexes;
    }

    public AppendEntriesResult appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm,
                                             ArrayList<Event> logEntries, int leaderCommit) {
        Boolean success = false;
        int[] resultLogStatus;
        if (!(term < currentTerm)) {
            resultLogStatus = logs.checkLogIndexWithTerm(prevLogIndex, prevLogTerm);
            if (!(resultLogStatus[0] == LogEntryStatus.NOT_FOUND)) {
                if (resultLogStatus[0] == LogEntryStatus.CONFLICT) {
                    logs.deleteEntriesFromIndex(resultLogStatus[1]);
                }
                logs.appendLogEntries(logEntries);
                if (leaderCommit < logs.getCommitIndex()) {
                    logs.setCommitIndex(Math.min(leaderCommit, logs.getIndexFromLastEntry()));
                    if (logs.getCommitIndex() > logs.getLastApplied()) {
                        //TODO Increment??
                        logs.setLastApplied(logs.getCommitIndex());
                        //TODO Apply log[lastApplied to state machine
                    }
                }
                success = true;
            }
        }
        if (term > currentTerm) {
            currentTerm = term;
            //TODO Update the state to follower if its the leader
        }
        return AppendEntriesResult.generateResponse(currentTerm, success);
    }

    public RequestVoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        Boolean voteGrantedFor = false;
        int[] resultLogStatus;
        if (!(term < currentTerm)) {
            if (votedFor != null) {
                resultLogStatus = logs.checkLogIndexWithTerm(lastLogIndex, lastLogTerm);
                //TODO is this logic correct?? is it only found or do we need to consider CONFLICT situations
                if (resultLogStatus[0] == LogEntryStatus.FOUND) {
                    voteGrantedFor = true;
                }
            }
        }
        if (term > currentTerm) {
            currentTerm = term;
            //TODO Update the state to follower if its the leader
        }
        return RequestVoteResult.generateResponse(currentTerm, voteGrantedFor);
    }

    public void informLeaderWhenRestart() {

    }

    public void removeRestartedServerDetails() {

    }

    @Override
    public void run() {

    }

    public void init() {

    }
}
