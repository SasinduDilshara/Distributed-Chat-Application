package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotOwnerException;

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

    public static ChatRoom createChatRoom(String roomId, ClientThread owner) {
        return new ChatRoom(roomId, owner);
    }

    private boolean isAClient(String clientId) {
        for(ClientThread existingClient: clients) {
            if(existingClient.getId().equals(clientId)) {
                return true;
            }
        }
        return false;
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

    public void addClient(ClientThread client) throws ClientAlreadyInChatRoomException {
        if(isAClient(client.getId())) {
            String errorMsg = ClientAlreadyInChatRoomException.generateClientAlreadyInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientAlreadyInChatRoomException(errorMsg);
        }
        this.clients.add(client);
        // notify all clients in the chat
        // notify the joinee
    }

    public void removeClient(ClientThread client) throws ClientNotInChatRoomException {
        if(!isAClient(client.getId())) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(
                    this.roomId, client.getId());
            throw new ClientNotInChatRoomException(errorMsg);
        }
        this.clients.remove(client);
        // TODO: inform clients about the removal
    }

    // send message
    public void sendMessage(String msg, String senderId) throws ClientNotInChatRoomException {
        if(!isAClient(senderId)) {
            String errorMsg = ClientNotInChatRoomException.generateClientNotInChatRoomMessage(senderId, this.roomId);
            throw new ClientNotInChatRoomException(errorMsg);
        }
        for(ClientThread client: clients) {
            if(client.getId().equals(owner.getId())) {
                continue;
            }
            // client receives the message.
            client.recieveMessage(msg);
        }
    }

    // delete room
    public void delete(String clientId) throws ClientNotOwnerException {
        if(!clientId.equals(this.owner.getId())) {
            String errorMsg = ClientNotOwnerException.generateClientNotOwnerMessage(clientId, roomId);
            throw new ClientNotOwnerException(errorMsg);
        }
        // TODO: Create a generator for all common message templates.
        String deleteMessage = "Room " +this.roomId+ " is deleted";
        for(ClientThread client: clients) {
            client.recieveMessage(deleteMessage);
        }
    }
}
