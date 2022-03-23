package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.Util;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class ChatRoom {
    private final ArrayList<ClientThread> clients;
    private final String roomId;
    private final ClientThread owner;

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
        return new ChatRoom(roomId, owner);
    }

    // create a mainhall at the server initiation
    public static ChatRoom createMainHall(String serverId) {
        return new ChatRoom(Util.getMainhall(serverId));
    }

    // check if given id belongs to a client in the room
    private boolean isAClient(String clientId) {
        for (ClientThread existingClient : clients) {
            if (existingClient.getId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    // return the arraylist of names of the clients in the room
    public ArrayList<String> getClientIds() {
        ArrayList<String> clientNames = new ArrayList<>();
        for (ClientThread client : clients) {
            clientNames.add(client.getId());
        }
        return clientNames;
    }

    // add a new client to the room
    public void addClientAndNotify(ClientThread client, String prevRoomName) throws ClientAlreadyInChatRoomException {
        if (isAClient(client.getId())) {
            String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientAlreadyInChatRoomException(errorMsg);
        }
        this.clients.add(client);
        for (ClientThread existingClient : clients) {
            existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
        }
    }

    public void addClient(ClientThread client, String prevRoomName) throws ClientAlreadyInChatRoomException {
        if (isAClient(client.getId())) {
            String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientAlreadyInChatRoomException(errorMsg);
        }
        for (ClientThread existingClient : clients) {
            existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
        }
        this.clients.add(client);
    }

    // add a set of new clients to the room
    public void addClients(ArrayList<ClientThread> newClients, String prevRoomName) throws ClientAlreadyInChatRoomException {
        for (ClientThread client : newClients) {
            if (isAClient(client.getId())) {
                String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                        this.roomId, client.getId());
                throw new ClientAlreadyInChatRoomException(errorMsg);
            }
            for (ClientThread existingClient : clients) {
                existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), prevRoomName, roomId));
            }
        }
        this.clients.addAll(newClients);

    }

    // remove a client from the room
    public void removeClient(ClientThread client, String nextRoomName) throws ClientNotInChatRoomException {
        if (!isAClient(client.getId())) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
                    client.getId(), this.roomId);
            throw new ClientNotInChatRoomException(errorMsg);
        }
        this.clients.remove(client);
        for (ClientThread existingClient : clients) {
            existingClient.sendResponse(ServerMessage.getRoomChangeResponse(client.getId(), roomId, nextRoomName));
        }
    }

    public void removeClient(ClientThread client) throws ClientNotInChatRoomException {
        if (!isAClient(client.getId())) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
                    client.getId(), this.roomId);
            throw new ClientNotInChatRoomException(errorMsg);
        }
        this.clients.remove(client);
    }

    public void sendMessage(String message, ClientThread sender) throws ClientNotInChatRoomException {
        for (ClientThread client : clients) {
            if (!client.equals(sender)) {
                client.sendResponse(ServerMessage.getMessageResponse(sender.getId(), message));
            }
        }
    }

}
