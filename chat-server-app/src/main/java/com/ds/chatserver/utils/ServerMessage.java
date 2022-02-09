package com.ds.chatserver.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;

public class ServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getNewIdentityResponse(Boolean approved) {
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "newidentity");
        newIdentity.put("approved", approved.toString());
        return newIdentity;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getListResponse(ArrayList<String> roomNames) {
        JSONObject list = new JSONObject();
        JSONArray rooms = new JSONArray();
        list.put("type", "roomlist");
        rooms.addAll(roomNames);
        list.put("rooms", rooms);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getWhoResponse(String roomId, ArrayList<String> clientNames, String owner) {
        JSONObject who = new JSONObject();
        JSONArray identities = new JSONArray();
        identities.addAll(clientNames);
        who.put("type", "who");
        who.put("room", roomId);
        who.put("identities", identities);
        who.put("owner", owner);
        return who;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoomResponse(String roomid, Boolean approved) {
        JSONObject create_room = new JSONObject();
        create_room.put("type", "createroom");
        create_room.put("roomid", roomid);
        create_room.put("approved", approved.toString());
        return create_room;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomChangeResponse(String identity, String former, String roomid) {
        JSONObject change_room = new JSONObject();
        change_room.put("type", "roomchange");
        change_room.put("identity", identity);
        change_room.put("former", former);
        change_room.put("roomid", roomid);
        return change_room;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRouteResponse(String roomid, String host, String port) {
        JSONObject route = new JSONObject();
        route.put("type", "route");
        route.put("roomid", roomid);
        route.put("host", host);
        route.put("port", port);
        return route;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerChangeResponse(String serverId) {
        JSONObject serverchange = new JSONObject();
        serverchange.put("type", "serverchange");
        serverchange.put("approved", "true");
        serverchange.put("serverid", serverId);
        return serverchange;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomResponse(String roomid, Boolean approved) {
        JSONObject delete = new JSONObject();
        delete.put("type", "deleteroom");
        delete.put("roomid", roomid);
        delete.put("approved", approved.toString());
        return delete;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getDeleteRoomServerResponse(String serverId, String roomId) {
        JSONObject server_delete = new JSONObject();
        server_delete.put("type", "deleteroom");
        server_delete.put("serverid", serverId);
        server_delete.put("roomId", roomId);
        return server_delete;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getMessageResponse(String identity, String content) {
        JSONObject message = new JSONObject();
        message.put("type", "message");
        message.put("identity", identity);
        message.put("content", content);
        return message;
    }
}
