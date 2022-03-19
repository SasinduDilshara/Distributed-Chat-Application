package com.ds.chatserver.utils;

import com.ds.chatserver.log.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.MOVE_JOIN;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.RequestTypeConstants.*;

public class ServerServerMessage {

    @SuppressWarnings("unchecked")
    private static JSONArray convertEntriesToJson(List<Event> entries) {
        JSONArray jsonEntries = new JSONArray();
        for(Event entry: entries) {
            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put(TERM, String.valueOf(entry.getLogTerm()));
            jsonEntry.put(LOG_INDEX, String.valueOf(entry.getLogIndex()));
            // type is converted from EventType to enum
            jsonEntry.put(TYPE, entry.getType().toString());
            jsonEntry.put(CLIENT_ID, entry.getClientId());
            jsonEntry.put(SERVER_ID, entry.getServerId());
            jsonEntry.put(PARAMETER, entry.getParameter());

            jsonEntries.add(jsonEntry);
        }
        return jsonEntries;
    }


    @SuppressWarnings("unchecked")
    public static JSONObject getRequestVoteRequest(int term, String candidateId, int lastLogIndex,
                                                   int lastLogTerm) {
        JSONObject requestVote = new JSONObject();
        requestVote.put(TYPE, REQUEST_VOTE);
        requestVote.put(TERM, String.valueOf(term));
        requestVote.put(CANDIDATE_ID, candidateId);
        requestVote.put(LAST_LOG_INDEX, String.valueOf(lastLogIndex));
        requestVote.put(LAST_LOG_TERM, String.valueOf(lastLogTerm));
        return requestVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRequestVoteResponse(int term, boolean voteGranted) {
        JSONObject requestVote = new JSONObject();
        requestVote.put(TYPE, REQUEST_VOTE);
        requestVote.put(TERM, String.valueOf(term));
        requestVote.put(VOTE_GRANTED, voteGranted);
        return requestVote;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getAppendEntriesRequest(int term, String leaderId, int prevLogIndex,
                                                     int prevLogTerm, List<Event> entries, int leaderCommit) {
        JSONObject appendEntries = new JSONObject();
        appendEntries.put(TYPE, APPEND_ENTRIES);
        appendEntries.put(TERM, String.valueOf(term));
        appendEntries.put(LEADER_ID, leaderId);
        appendEntries.put(PREVIOUS_LOG_INDEX, String.valueOf(prevLogIndex));
        appendEntries.put(PREVIOUS_LOG_TERM, String.valueOf(prevLogTerm));
        appendEntries.put(ENTRIES, convertEntriesToJson(entries));
        appendEntries.put(LEADER_COMMIT, String.valueOf(leaderCommit));
        return appendEntries;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getAppendEntriesResponse(int term, boolean success) {
        JSONObject appendEntries = new JSONObject();
        appendEntries.put(TYPE, APPEND_ENTRIES);
        appendEntries.put(TERM, String.valueOf(term));
        appendEntries.put(SUCCESS, success);
        return appendEntries;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateClientRequest(int term, String clientId, String senderId) {
        JSONObject createClient = new JSONObject();
        createClient.put(TYPE, CREATE_CLIENT);
        createClient.put(TERM, String.valueOf(term));
        createClient.put(CLIENT_ID, clientId);
        createClient.put(SENDER_ID, senderId);
        return createClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateClientResponse(int term, boolean success) {
        JSONObject createClient = new JSONObject();
        createClient.put(TYPE, CREATE_CLIENT);
        createClient.put(TERM, String.valueOf(term));
        createClient.put(SUCCESS, success);
        return createClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteClientRequest(int term, String clientId, String senderId) {
        JSONObject deleteClient = new JSONObject();
        deleteClient.put(TYPE, DELETE_CLIENT);
        deleteClient.put(TERM, String.valueOf(term));
        deleteClient.put(CLIENT_ID, clientId);
        deleteClient.put(SENDER_ID, senderId);
        return deleteClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteClientResponse(int term, boolean success) {
        JSONObject deleteClient = new JSONObject();
        deleteClient.put(TYPE, DELETE_CLIENT);
        deleteClient.put(TERM, String.valueOf(term));
        deleteClient.put(SUCCESS, success);
        return deleteClient;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateChatroomRequest(int term, String clientId, String roomId, String senderId) {
        JSONObject createRoom = new JSONObject();
        createRoom.put(TYPE, CREATE_CHAT_ROOM);
        createRoom.put(TERM, String.valueOf(term));
        createRoom.put(CLIENT_ID, clientId);
        createRoom.put(ROOM_ID, roomId);
        createRoom.put(SENDER_ID, senderId);
        return createRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateChatroomResponse(int term, boolean success) {
        JSONObject createRoom = new JSONObject();
        createRoom.put(TYPE, CREATE_CHAT_ROOM);
        createRoom.put(TERM, String.valueOf(term));
        createRoom.put(SUCCESS, success);
        return createRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomRequest(int term, String clientId, String senderId, String roomId) {
        JSONObject deleteRoom = new JSONObject();
        deleteRoom.put(TYPE, DELETE_CHAT_ROOM);
        deleteRoom.put(TERM, String.valueOf(term));
        deleteRoom.put(CLIENT_ID, clientId);
        deleteRoom.put(SENDER_ID, senderId);
        deleteRoom.put(ROOM_ID, roomId);
        return deleteRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomResponse(int term, boolean success) {
        JSONObject deleteRoom = new JSONObject();
        deleteRoom.put(TYPE, DELETE_CHAT_ROOM);
        deleteRoom.put(TERM, String.valueOf(term));
        deleteRoom.put(SUCCESS, success);
        return deleteRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getChangeRoomRequest(int term, String clientId, String former, String roomId, String senderId) {
        JSONObject changeRoom = new JSONObject();
        changeRoom.put(TYPE, CHANGE_ROOM);
        changeRoom.put(TERM, String.valueOf(term));
        changeRoom.put(CLIENT_ID, clientId);
        changeRoom.put(FORMER, former);
        changeRoom.put(ROOM_ID, roomId);
        changeRoom.put(SENDER_ID, senderId);
        return changeRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getChangeRoomResponse(int term, boolean success, String serverId) {
        JSONObject changeRoom = new JSONObject();
        changeRoom.put(TYPE, CHANGE_ROOM);
        changeRoom.put(TERM, String.valueOf(term));
        changeRoom.put(SUCCESS, success);
        changeRoom.put(SERVER_ID, serverId);
        return changeRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerInitRequest(int term, String serverId) {
        JSONObject serverInit = new JSONObject();
        serverInit.put(TYPE, SERVER_INIT);
        serverInit.put(TERM, String.valueOf(term));
        serverInit.put(SERVER_ID, serverId);
        return serverInit;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerInitResponse(int term, boolean success) {
        JSONObject serverInit = new JSONObject();
        serverInit.put(TYPE, SERVER_INIT);
        serverInit.put(TERM, String.valueOf(term));
        serverInit.put(SUCCESS, success);
        return serverInit;
    }

    public static JSONObject getMoveJoinRequest(int term, String clientId, String former, String roomId, String senderId) {
        JSONObject moveJoinRoom = new JSONObject();
        moveJoinRoom.put(TYPE, MOVE_JOIN);
        moveJoinRoom.put(TERM, String.valueOf(term));
        moveJoinRoom.put(CLIENT_ID, clientId);
        moveJoinRoom.put(FORMER, former);
        moveJoinRoom.put(ROOM_ID, roomId);
        moveJoinRoom.put(SENDER_ID, senderId);
        return moveJoinRoom;
    }

    public static JSONObject getMoveJoinResponse(int term, String roomId, boolean success) {
        JSONObject moveJoinRoom = new JSONObject();
        moveJoinRoom.put(TYPE, MOVE_JOIN);
        moveJoinRoom.put(TERM, String.valueOf(term));
        moveJoinRoom.put(ROOM_ID, roomId);
        moveJoinRoom.put(SUCCESS, success);
        return moveJoinRoom;
    }
}
