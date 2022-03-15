package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotOwnerException;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.Util;

import java.util.ArrayList;

public class ChatRoom {
    private final ArrayList<ClientThread> clients;
    private final String roomId;
    private final ClientThread owner;
    private Server server;

    // constructor for normal chatroom
    private ChatRoom(String roomId, ClientThread owner) {
        this.roomId = roomId;
        this.owner = owner;
        clients = new ArrayList<>();
        clients.add(owner);
    }

    // constructor for mainhall
    private ChatRoom(String roomId) {
        this.roomId = roomId;
        this.owner = null;
        clients = new ArrayList<>();
    }

    // create chatroom by a user
    public static ChatRoom createChatRoom(String roomId, ClientThread owner) {
        // TODO: if roomId already exists send user approved: false.
        return new ChatRoom(roomId, owner);
    }

    // create a mainhall at the server initiation
    public static ChatRoom createMainHall(String serverId) {
        // TODO: Get the server id and append to the mainhall name. eg- MainHall-s1
        return new ChatRoom(Util.getMainhall(serverId));
    }

    // check if given id belongs to a client in the room
    private boolean isAClient(String clientId) {
        for(ClientThread existingClient: clients) {
            if(existingClient.getId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    // return the arraylist of names of the clients in the room
    public ArrayList<String> getClientIds() {
        ArrayList<String> clientNames = new ArrayList<>();
        for(ClientThread client: clients) {
            clientNames.add(client.getId());
        }
        return clientNames;
    }

    // add a new client to the room
    public void addClient(ClientThread client, String prevRoomName) throws ClientAlreadyInChatRoomException {
        if(isAClient(client.getId())) {
            String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientAlreadyInChatRoomException(errorMsg);
        }
        this.clients.add(client);
        for(ClientThread existingClient: clients) {
            existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
        }

    }

    // add a set of new clients to the room
    public void addClients(ArrayList<ClientThread> newClients, String prevRoomName) throws ClientAlreadyInChatRoomException {
        for(ClientThread client: newClients) {
            if(isAClient(client.getId())) {
                String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                        this.roomId, client.getId());
                throw new ClientAlreadyInChatRoomException(errorMsg);
            }
            for(ClientThread existingClient: clients) {
                existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
            }
        }
        this.clients.addAll(newClients);

    }

    // remove a client from the room
    public void removeClient(ClientThread client, String nextRoomName) throws ClientNotInChatRoomException {
        if(!isAClient(client.getId())) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientNotInChatRoomException(errorMsg);
        }
        this.clients.remove(client);
        for(ClientThread existingClient: clients) {
            existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), roomId, nextRoomName));
        }
    }

    public void removeClient(ClientThread client) throws ClientNotInChatRoomException {
        if(!isAClient(client.getId())) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientNotInChatRoomException(errorMsg);
        }
        this.clients.remove(client);
    }

    // send a message to all the members of a room
    public void sendMessage(String message, String senderId) throws ClientNotInChatRoomException {
        if(!isAClient(senderId)) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(senderId, this.roomId);
            throw new ClientNotInChatRoomException(errorMsg);
        }
        for(ClientThread client: clients) {
            if(!client.getId().equals(senderId)) {
                client.sendResponse(ServerMessage.getMessageResponse(senderId, message));
            }
        }
    }

    // inform all the members of the room about the deletion of the room
    public void deleteRoom(String clientId, String mainHallId) throws ClientNotOwnerException {
        if(this.owner == null || !clientId.equals(this.owner.getId())) {
            String errorMsg = ClientNotOwnerException.generateClientNotOwnerMessage(clientId, roomId);
            throw new ClientNotOwnerException(errorMsg);
        }
        for(ClientThread client: clients) {
            if(!clientId.equals(this.owner.getId())) {
                client.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), roomId, mainHallId));
            }
        }
    }

    public ArrayList<ClientThread> getClients() {
        return clients;
    }

    public String getRoomId() {
        return roomId;
    }

    public ClientThread getOwner() {
        return owner;
    }
}
