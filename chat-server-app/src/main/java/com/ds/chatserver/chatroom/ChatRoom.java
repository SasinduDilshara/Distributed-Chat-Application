package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotOwnerException;
import com.ds.chatserver.jsonparser.ServerMessage;

import java.util.ArrayList;

public class ChatRoom {
    private final ArrayList<ClientThread> clients;
    private final String roomId;
    private final ClientThread owner;

    private ChatRoom(String roomId, ClientThread owner) {
        this.roomId = roomId;
        this.owner = owner;
        clients = new ArrayList<>();
        clients.add(owner);
    }

//    private boolean isAClient(String clientId) {
//        for(ClientThread existingClient: clients) {
//            if(existingClient.getId().equals(clientId)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private ArrayList<String> getClientNames() {
//        ArrayList<String> clientNames = new ArrayList<>();
//        for(ClientThread client: clients) {
//            clientNames.add(client.getId());
//        }
//        return clientNames;
//    }
//
//    public ArrayList<ClientThread> getClients() {
//        return clients;
//    }
//
//    public String getRoomId() {
//        return roomId;
//    }
//
//    public ClientThread getOwner() {
//        return owner;
//    }
//
//    public void listClients(ClientThread client) {
//        client.generateResponse(ServerMessage.getWhoResponse(roomId, getClientNames(), owner.getId()));
//    }
//
//
//    public static ChatRoom createChatRoom(String roomId, ClientThread owner) {
//        // TODO: if roomId already exists send user approved: false.
//        owner.generateResponse(ServerMessage.getCreateRoomResponse(roomId, true));
//        return new ChatRoom(roomId, owner);
//    }
//
//    public void addClient(ClientThread client, String prevRoomName) throws ClientAlreadyInChatRoomException {
//        if(isAClient(client.getId())) {
//            String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
//                    this.roomId, client.getId());
//            throw new ClientAlreadyInChatRoomException(errorMsg);
//        }
//        this.clients.add(client);
//        for(ClientThread existingClient: clients) {
//            existingClient.generateResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
//        }
//    }
//
//    public void removeClient(ClientThread client, String nextRoomName) throws ClientNotInChatRoomException {
//        if(!isAClient(client.getId())) {
//            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
//                    this.roomId, client.getId());
//            throw new ClientNotInChatRoomException(errorMsg);
//        }
//        this.clients.remove(client);
//        for(ClientThread existingClient: clients) {
//            existingClient.generateResponse(ServerMessage.getRoomChangeResponse(client.getId(), roomId, nextRoomName));
//        }
//    }
//
//    public void sendMessage(String message, String senderId) throws ClientNotInChatRoomException {
//        if(!isAClient(senderId)) {
//            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(senderId, this.roomId);
//            throw new ClientNotInChatRoomException(errorMsg);
//        }
//        for(ClientThread client: clients) {
//            if(!client.getId().equals(owner.getId())) {
//                client.generateResponse(ServerMessage.getMessageResponse(senderId, message));
//            }
//        }
//    }
//
//    // delete room
//    public void deleteRoom(String ownerId, String mainHallId) throws ClientNotOwnerException {
//        if(!ownerId.equals(this.owner.getId())) {
//            String errorMsg = ClientNotOwnerException.generateClientNotOwnerMessage(ownerId, roomId);
//            throw new ClientNotOwnerException(errorMsg);
//        }
//        for(ClientThread client: clients) {
//            if(client.getId().equals(ownerId)) {
//                client.generateResponse(ServerMessage.getDeleteRoomResponse(roomId, true));
//            } else {
//                client.generateResponse(ServerMessage.getRoomChangeResponse(client.getId(), roomId, mainHallId));
//            }
//        }
//    }
}
