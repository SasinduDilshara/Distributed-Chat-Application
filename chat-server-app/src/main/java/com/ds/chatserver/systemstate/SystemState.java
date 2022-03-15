package com.ds.chatserver.systemstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.Util;

import java.util.HashMap;
import java.util.HashSet;

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
                case ROUTE:
                    commitRoute(event);
                    break;
            }

            server.getRaftLog().incrementLastApplied();

        }
    }

    private static void commitNewIdentity(Event event){
        String clientId = event.getClientId();
        String chatroomId = Util.getMainhall(event.getServerId());
        String serverId = event.getServerId();
        ClientLog clientLog = new ClientLog(clientId, chatroomId, serverId);

        clientLists.put(clientId, clientLog);
        chatroomLists.get(chatroomId).addParticipant(clientId);
    }

    private static void commitCreateRoom(Event event){
        String clientId = event.getClientId();
        String newChatroomId = event.getParameter();
        String previousChatroomId = clientLists.get(clientId).getChatroomName();

        clientLists.get(clientId).setChatroomName(newChatroomId);

        ChatroomLog chatroomLog = new ChatroomLog(newChatroomId, clientId, event.getServerId());
        chatroomLog.addParticipant(clientId);
        chatroomLists.get(previousChatroomId).removeParticipant(clientId);
        chatroomLists.put(newChatroomId, chatroomLog);
    }

    private static void commitJoinRoom(Event event){
        String clientId = event.getClientId();
        String newChatroomId = event.getParameter();
        String previousChatroomId = clientLists.get(clientId).getChatroomName();
        String newChatroomServerId = chatroomLists.get(newChatroomId).getServerId();

        /*
         * Client is in the same server where the new chatroom exists
         */
        if(newChatroomServerId.equals(clientLists.get(clientId).getServerId())){
            clientLists.get(clientId).setChatroomName(newChatroomId);
            chatroomLists.get(newChatroomId).addParticipant(clientId);
        }
        else{
            clientLists.get(clientId).setChatroomName("");
            clientLists.get(clientId).setServerId("");
        }

        chatroomLists.get(previousChatroomId).removeParticipant(clientId);

    }

    private static void commitDeleteRoom(Event event){
        String chatroomId = event.getParameter();
        String mainHallId = Util.getMainhall(chatroomLists.get(chatroomId).getServerId());
        HashSet<String> participants = chatroomLists.get(chatroomId).getParticipants();

        for(String participant : participants){
            clientLists.get(participant).setChatroomName(mainHallId);
            chatroomLists.get(chatroomId).removeParticipant(participant);
            chatroomLists.get(mainHallId).addParticipant(participant);
        }

        chatroomLists.remove(chatroomId);
    }

    private static void commitQuit(Event event){
        String clientId = event.getClientId();
        String chatroomId = clientLists.get(clientId).getChatroomName();
        String mainHallId = Util.getMainhall(chatroomLists.get(chatroomId).getServerId());
        /*
        * client is the owner of that chat room
        */
        if(chatroomLists.get(chatroomId).getOwnerId().equals(clientId)){
            HashSet<String> participants = chatroomLists.get(chatroomId).getParticipants();

            for(String participant : participants){
                if(participant.equals(clientId)){
                    continue;
                }
                clientLists.get(participant).setChatroomName(mainHallId);
                chatroomLists.get(chatroomId).removeParticipant(participant);
                chatroomLists.get(mainHallId).addParticipant(participant);
            }

            chatroomLists.remove(chatroomId);
        }
        else{
            chatroomLists.get(chatroomId).removeParticipant(clientId);
        }
        clientLists.remove(clientId);
    }

    private static void commitRoute(Event event) {
        String clientId = event.getClientId();
        String chatroomId = event.getParameter();

        clientLists.get(clientId).setChatroomName(chatroomId);
        clientLists.get(clientId).setServerId(event.getServerId());

        chatroomLists.get(chatroomId).addParticipant(clientId);
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

    public static Boolean isOwner(String clientId) {
        String chatroomId = clientLists.get(clientId).getChatroomName();
        return chatroomLists.get(chatroomId).getOwnerId().equals(clientId);
    }
}
