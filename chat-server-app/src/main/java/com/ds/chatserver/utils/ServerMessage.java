package com.ds.chatserver.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.*;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

public class ServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getNewIdentityResponse(Boolean approved) {
        JSONObject newIdentity = new JSONObject();
        newIdentity.put(TYPE, NEW_IDENTITY);
        newIdentity.put(APPROVED, approved.toString());
        return newIdentity;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomListResponse(ArrayList<String> roomNames) {
        JSONObject list = new JSONObject();
        JSONArray rooms = new JSONArray();
        list.put(TYPE, ROOM_LIST);
        rooms.addAll(roomNames);
        list.put(ROOMS, rooms);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getWhoResponse(String roomId, ArrayList<String> clientNames, String owner) {
        JSONObject who = new JSONObject();
        JSONArray identities = new JSONArray();
        identities.addAll(clientNames);
        who.put(TYPE, ROOM_CONTENTS);
        who.put(ROOM_ID, roomId);
        who.put(IDENTITIES, identities);
        who.put(OWNER, owner);
        return who;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoomResponse(String roomid, Boolean approved) {
        JSONObject create_room = new JSONObject();
        create_room.put(TYPE, CREATE_ROOM);
        create_room.put(ROOM_ID, roomid);
        create_room.put(APPROVED, approved.toString());
        return create_room;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomChangeResponse(String identity, String former, String roomid) {
        JSONObject change_room = new JSONObject();
        change_room.put(TYPE, ROOM_CHANGE);
        change_room.put(IDENTITY, identity);
        change_room.put(FORMER, former);
        change_room.put(ROOM_ID, roomid);
        return change_room;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRouteResponse(String roomId, String host, String port) {
        JSONObject route = new JSONObject();
        route.put(TYPE, ROUTE);
        route.put(ROOM_ID, roomId);
        route.put(HOST, host);
        route.put(PORT, port);
        return route;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerChangeResponse(Boolean approved ,String serverId) {
        JSONObject serverchange = new JSONObject();
        serverchange.put(TYPE, SERVER_CHANGE);
        serverchange.put(APPROVED, approved.toString());
        serverchange.put(SERVER_ID, serverId);
        return serverchange;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomResponse(String roomId, Boolean approved) {
        JSONObject delete = new JSONObject();
        delete.put(TYPE, DELETE_ROOM);
        delete.put(ROOM_ID, roomId);
        delete.put(APPROVED, approved.toString());
        return delete;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomServerResponse(String serverId, String roomId) {
        JSONObject server_delete = new JSONObject();
        server_delete.put(TYPE, DELETE_ROOM);
        server_delete.put(SERVER_ID, serverId);
        server_delete.put(ROOM_ID, roomId);
        return server_delete;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getMessageResponse(String identity, String content) {
        JSONObject message = new JSONObject();
        message.put(TYPE, MESSAGE);
        message.put(IDENTITY, identity);
        message.put(CONTENT, content);
        return message;
    }

    public static JSONObject getJsonResponses(ArrayList<JSONObject> jsonObjects) {
        JSONObject message = new JSONObject();
        for (int i = 0; i < jsonObjects.size(); i++) {
            message.put(String.valueOf(i), jsonObjects.get(i));
        }
        return message;
    }
}
