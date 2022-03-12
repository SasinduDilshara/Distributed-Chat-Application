package com.ds.chatserver.systemstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.Util;

import java.util.HashMap;

public class SystemState {
    private static HashMap<String, ClientLog> clientLists = new HashMap<>();
    private static HashMap<String, ChatroomLog> chatroomLists = new HashMap<>();

    public synchronized static void commit(Server server){
        System.out.println("Commiting.. commit index: " + server.getRaftLog().getCommitIndex());
        for(int i = server.getRaftLog().getLastApplied()+1; i <= server.getRaftLog().getCommitIndex(); i++){
            Event event = server.getRaftLog().getIthEvent(i);

            EventType eventType = event.getType();

            switch (eventType){
                case NEW_IDENTITY:
                    commitNewIdentity(event);
                    break;
                case CREATE_ROOM:
                    commitCreateRoom(event);
                    break;
                case JOIN_ROOM:
                    commitJoinRoom(event);
                    break;
                case DELETE_ROOM:
                    commitDeleteRoom(event);
                    break;
                case QUIT:
                    commitQuit(event);
                    break;

                case SERVER_CHANGE:
                    commitServerChange(event);
                    break;
            }

            server.getRaftLog().incrementLastApplied();

        }
    }

    private static void commitNewIdentity(Event event){
        clientLists.put(event.getClientId(), new ClientLog(event.getClientId(),
                Util.getMainhall(event.getServerId()),
                event.getServerId()));
        chatroomLists.get(Util.getMainhall(event.getServerId())).addParticipant(event.getClientId());
    }

    private static void commitCreateRoom(Event event){

    }

    private static void commitJoinRoom(Event event){

    }

    private static void commitDeleteRoom(Event event){

    }

    private static void commitQuit(Event event){

    }

    private static void commitServerChange(Event event){

    }

    public static void addChatroom(ChatroomLog chatroomLog){
        chatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
    }

    public static Boolean isClientExist(String clientId) {
        return clientLists.containsKey(clientId);
    }

    public static Boolean isChatroomExist(String chatroomName) {
        return chatroomLists.containsKey(chatroomName);
    }

}
