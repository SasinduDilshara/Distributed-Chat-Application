package com.ds.chatserver.systemstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class SystemState {
    private static HashMap<String, ClientLog> clientLists = new HashMap<>();
    private static HashMap<String, ChatroomLog> chatroomLists = new HashMap<>();

    public synchronized static void commit(Server server) {
        log.debug("Commiting.. commit index: {}", server.getRaftLog().getCommitIndex());
        for (int i = server.getRaftLog().getLastApplied() + 1; i <= server.getRaftLog().getCommitIndex(); i++) {
            Event event = server.getRaftLog().getIthEvent(i);

            EventType eventType = event.getType();

            switch (eventType) {
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
                case SERVER_INIT:
                    commitServerInit(event);
                    break;
            }
            log.debug(event.toString());
            server.getRaftLog().incrementLastApplied();
        }
    }

    private static void commitNewIdentity(Event event) {
        String clientId = event.getClientId();
        String chatroomId = Util.getMainhall(event.getServerId());
        String serverId = event.getServerId();
        ClientLog clientLog = new ClientLog(clientId, chatroomId, serverId);

        clientLists.put(clientId, clientLog);
        chatroomLists.get(chatroomId).addParticipant(clientId);
    }

    private static void commitCreateRoom(Event event) {
        String clientId = event.getClientId();
        String newChatroomId = event.getParameter();
        String previousChatroomId = clientLists.get(clientId).getChatroomName();

        clientLists.get(clientId).setChatroomName(newChatroomId);

        ChatroomLog chatroomLog = new ChatroomLog(newChatroomId, clientId, event.getServerId());
        chatroomLog.addParticipant(clientId);
        chatroomLists.get(previousChatroomId).removeParticipant(clientId);
        chatroomLists.put(newChatroomId, chatroomLog);
    }

    private static void commitJoinRoom(Event event) {
        String clientId = event.getClientId();
        String newChatroomId = event.getParameter();
        String previousChatroomId = clientLists.get(clientId).getChatroomName();
        String newChatroomServerId = chatroomLists.get(newChatroomId).getServerId();

        /*
         * Client is in the same server where the new chatroom exists
         */
        if (newChatroomServerId.equals(clientLists.get(clientId).getServerId())) {
            clientLists.get(clientId).setChatroomName(newChatroomId);
            chatroomLists.get(newChatroomId).addParticipant(clientId);
        } else {
            clientLists.get(clientId).setChatroomName("");
            clientLists.get(clientId).setServerId("");
        }

        chatroomLists.get(previousChatroomId).removeParticipant(clientId);

    }

    private static void commitDeleteRoom(Event event) {
        String chatroomId = event.getParameter();
        String mainHallId = Util.getMainhall(chatroomLists.get(chatroomId).getServerId());
        HashSet<String> participants = chatroomLists.get(chatroomId).getParticipants();

        for (String participant : participants) {
            clientLists.get(participant).setChatroomName(mainHallId);
            chatroomLists.get(chatroomId).removeParticipant(participant);
            chatroomLists.get(mainHallId).addParticipant(participant);
        }

        chatroomLists.remove(chatroomId);
    }

    private static void commitQuit(Event event) {
        String clientId = event.getClientId();
        String chatroomId = clientLists.get(clientId).getChatroomName();
        String mainHallId = Util.getMainhall(chatroomLists.get(chatroomId).getServerId());
        /*
         * client is the owner of that chat room
         */
        if (chatroomLists.get(chatroomId).getOwnerId().equals(clientId)) {
            HashSet<String> participants = chatroomLists.get(chatroomId).getParticipants();

            for (String participant : participants) {
                if (participant.equals(clientId)) {
                    continue;
                }
                clientLists.get(participant).setChatroomName(mainHallId);
                chatroomLists.get(chatroomId).removeParticipant(participant);
                chatroomLists.get(mainHallId).addParticipant(participant);
            }

            chatroomLists.remove(chatroomId);
        } else {
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

    private static void commitServerInit(Event event) {
        String initiatedServerId = event.getServerId();
        ArrayList<String> clientIds = new ArrayList<>(clientLists.keySet());
        ArrayList<String> roomIds = new ArrayList<>(chatroomLists.keySet());

        for (String client : clientIds) {
            if (clientLists.get(client).getServerId().equals(initiatedServerId)) {
                clientLists.remove(client);
            }
        }

        for (String roomId : roomIds) {
            if (roomId.equals(Util.getMainhall(chatroomLists.get(roomId).getServerId()))) {
                continue;
            }
            if (chatroomLists.get(roomId).getServerId().equals(initiatedServerId)) {
                chatroomLists.remove(roomId);
            }
        }
    }

    public synchronized static void addChatroom(ChatroomLog chatroomLog) {
        chatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
    }

    public synchronized static Boolean isClientExist(String clientId) {
        return clientLists.containsKey(clientId);
    }

    public synchronized static Boolean isChatroomExist(String chatroomName) {
        return chatroomLists.containsKey(chatroomName);
    }

    public synchronized static ChatroomLog getChatroomFromName(String chatroomName) {
        return chatroomLists.get(chatroomName);
    }

    public synchronized static Boolean isOwner(String clientId) {
        String chatroomId = clientLists.get(clientId).getChatroomName();
        return chatroomLists.get(chatroomId).getOwnerId().equals(clientId);
    }

    public synchronized static ArrayList<String> getChatRooms() {
        return new ArrayList<>(SystemState.chatroomLists.keySet());
    }

    public synchronized static ArrayList<String> getClients() {
        return new ArrayList<>(SystemState.clientLists.keySet());
    }

    public synchronized static Boolean checkOwnerFromChatroom(String chatroomId, String clientId) {
        ChatroomLog chatroom = chatroomLists.get(chatroomId);
        if (chatroom == null) {
            return false;
        }
        return chatroom.getOwnerId().equals(clientId);
    }

    public synchronized static String getChatroomServer(String chatroomId) {
        ChatroomLog chatroom = chatroomLists.get(chatroomId);
        return chatroom.getServerId();
    }

    public synchronized static Boolean isMemberOfChatroom(String clientId, String chatroomId) {
        if (!chatroomLists.containsKey(chatroomId)) {
            return false;
        }
        return chatroomLists.get(chatroomId).getParticipants().contains(clientId);
    }

    public synchronized static String getCurrentChatroomOfClient(String clientId) {
        if (!clientLists.containsKey(clientId)) {
            return "";
        }
        return clientLists.get(clientId).getChatroomName();
    }
}
