package com.ds.chatserver.utils;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ServerServerMessage {

    @SuppressWarnings("unchecked")
    private static JSONArray convertEntriesToJson(ArrayList<Event> entries) {
        JSONArray jsonEntries = new JSONArray();
        for(Event entry: entries) {
            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put("term", entry.getTerm());
            jsonEntry.put("logIndex", entry.getLogIndex());
            jsonEntry.put("type", entry.getType());
            jsonEntry.put("clientId", entry.getClientId());
            jsonEntry.put("serverId", entry.getServerId());

            jsonEntries.add(jsonEntry);
        }
        return jsonEntries;
    }


    @SuppressWarnings("unchecked")
    public static JSONObject getRequestVoteRequest(int term, String candidateId, int lastLogIndex,
                                                    int lastLogTerm) {
        JSONObject requestVote = new JSONObject();
        requestVote.put("type", "requestvoterequest");
        requestVote.put("term", term);
        requestVote.put("candidateid", candidateId);
        requestVote.put("lastlogindex", lastLogIndex);
        requestVote.put("lastlogterm", lastLogTerm);
        return requestVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRequestVoteResponse(int term, boolean voteGranted) {
        JSONObject requestVote = new JSONObject();
        requestVote.put("type", "requestvoteresponse");
        requestVote.put("term", term);
        requestVote.put("votegranted", voteGranted);
        return requestVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getAppendEntriesRequest(int term, String leaderId, int prevLogIndex,
                                                     int prevLogTerm, ArrayList<Event> entries, int leaderCommit) {
        JSONObject appendEntries = new JSONObject();
        appendEntries.put("type", "appendentriesrequest");
        appendEntries.put("term", term);
        appendEntries.put("leaderid", leaderId);
        appendEntries.put("prevlogindex", prevLogIndex);
        appendEntries.put("prevlogterm", prevLogTerm);
        appendEntries.put("entries", convertEntriesToJson(entries));
        appendEntries.put("leadercommit", leaderCommit);
        return appendEntries;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getAppendEntriesResponse(int term, boolean success) {
        JSONObject appendEntries = new JSONObject();
        appendEntries.put("type", "appendentriesresponse");
        appendEntries.put("term", term);
        appendEntries.put("success", success);
        return appendEntries;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateClientRequest(int term, String clientId, String senderId) {
        JSONObject createClient = new JSONObject();
        createClient.put("type", "requestcreateclient");
        createClient.put("term", term);
        createClient.put("clientid", clientId);
        createClient.put("senderid", senderId);
        return createClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateClientResponse(int term, boolean success) {
        JSONObject createClient = new JSONObject();
        createClient.put("type", "approvecreateclient");
        createClient.put("term", term);
        createClient.put("success", success);
        return createClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteClientRequest(int term, String clientId, String senderId) {
        JSONObject deleteClient = new JSONObject();
        deleteClient.put("type", "requestdeleteclient");
        deleteClient.put("term", term);
        deleteClient.put("clientid", clientId);
        deleteClient.put("senderid", senderId);
        return deleteClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteClientResponse(int term, boolean success) {
        JSONObject deleteClient = new JSONObject();
        deleteClient.put("type", "approvedeleteclient");
        deleteClient.put("term", term);
        deleteClient.put("success", success);
        return deleteClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateChatroomRequest(int term, String clientId, String roomId, String senderId) {
        JSONObject createRoom = new JSONObject();
        createRoom.put("type", "requestcreatechatroom");
        createRoom.put("term", term);
        createRoom.put("clientid", clientId);
        createRoom.put("roomid", roomId);
        createRoom.put("senderid", senderId);
        return createRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateChatroomResponse(int term, boolean success) {
        JSONObject createRoom = new JSONObject();
        createRoom.put("type", "approvecreatechatroom");
        createRoom.put("term", term);
        createRoom.put("success", success);
        return createRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomRequest(int term, String clientId, String senderId) {
        JSONObject deleteRoom = new JSONObject();
        deleteRoom.put("type", "requestdeleteroom");
        deleteRoom.put("term", term);
        deleteRoom.put("clientid", clientId);
        deleteRoom.put("senderid", senderId);
        return deleteRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomResponse(int term, boolean success) {
        JSONObject deleteRoom = new JSONObject();
        deleteRoom.put("type", "approvedeleteroom");
        deleteRoom.put("term", term);
        deleteRoom.put("success", success);
        return deleteRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getChangeRoomRequest(int term, String clientId, String former, String roomId, String senderId) {
        JSONObject changeRoom = new JSONObject();
        changeRoom.put("type", "requestchangeroom");
        changeRoom.put("term", term);
        changeRoom.put("clientid", clientId);
        changeRoom.put("former", former);
        changeRoom.put("roomid", roomId);
        changeRoom.put("senderid", senderId);
        return changeRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getChangeRoomResponse(int term, boolean success) {
        JSONObject changeRoom = new JSONObject();
        changeRoom.put("type", "approvechangeroom");
        changeRoom.put("term", term);
        changeRoom.put("success", success);
        return changeRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerInitRequest(int term, String serverId) {
        JSONObject serverInit = new JSONObject();
        serverInit.put("type", "requestserverinit");
        serverInit.put("term", term);
        serverInit.put("serverid", serverId);
        return serverInit;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerInitResponse(int term, boolean success) {
        JSONObject serverInit = new JSONObject();
        serverInit.put("type", "responseserverinit");
        serverInit.put("term", term);
        serverInit.put("success", success);
        return serverInit;
    }
}
