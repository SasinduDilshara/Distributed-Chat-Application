package com.ds.chatserver.utils;

import com.ds.chatserver.log.EventTypeName;
import org.json.simple.JSONObject;

import java.util.List;

public class ServerServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject requestVote(
            int term,
            String candidateId,
            int lastLogIndex,
            int lastLogTerm
    ) {
        JSONObject requestVote = new JSONObject();
        requestVote.put("type", "requestVote");
        requestVote.put("term", Integer.toString(term) );
        requestVote.put("candidateId", candidateId);
        requestVote.put("lastLogIndex", Integer.toString(lastLogIndex));
        requestVote.put("lastLogTerm", Integer.toString(lastLogTerm));
        return requestVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject responseVote(
            int term,
            boolean voteGranted
    ) {
        JSONObject responseVote = new JSONObject();
        responseVote.put("type", "requestVote");
        responseVote.put("term", Integer.toString(term));
        responseVote.put("voteGranted", voteGranted);
        return responseVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject requestAppendEntries (
            int term,
            String leaderId,
            int prevLogIndex,
            int prevLogTerm,
            List<EventTypeName> entries,
            int leaderCommit
    ) {
        JSONObject responseVote = new JSONObject();
        responseVote.put("type", "appendEntries");
        responseVote.put("term", Integer.toString(term));
        responseVote.put("leaderId", leaderId);
        responseVote.put("prevLogIndex", Integer.toString(prevLogIndex));
        responseVote.put("prevLogTerm", Integer.toString(prevLogTerm));
        responseVote.put("entries", entries);
        responseVote.put("leaderCommit", Integer.toString(leaderCommit));
        return responseVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject responseAppendEntries (
            int term,
            boolean success
    ) {
        JSONObject responseVote = new JSONObject();
        responseVote.put("type", "appendEntries");
        responseVote.put("term", Integer.toString(term));
        responseVote.put("success", success);
        return responseVote;
    }

}
