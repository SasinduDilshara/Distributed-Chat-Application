package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.jsonparser.ServerMessage;
import com.ds.chatserver.serverhandler.Server;

import java.util.ArrayList;

public class ChatRoomHandler {
    private ArrayList<ChatRoom> chatrooms;
    private static ChatRoomHandler chatRoomHandler = getInstance();

    private static ChatRoomHandler getInstance() {
        return new ChatRoomHandler();
    }

    private ChatRoomHandler() {
        this.chatrooms = new ArrayList<>();
    }

    private Boolean validateChatroomName(String name) {
        //TODO Implement
        return true;
    }

    public Boolean validateChatRoom(String name) {
        return (Server.validateChatroom(name));
    }

    public Boolean createChatRoom(String name, ClientThread clientThread)
            throws ChatroomAlreadyExistsException, InvalidChatroomException {
        if (!(validateChatRoom(name))) {
            throw new ChatroomAlreadyExistsException(name);
        } else if (!(validateChatroomName(name))) {
            throw new InvalidChatroomException(name);
        } else {
            getChatrooms().add(new ChatRoom(name, clientThread));
            return true;
        }
    }

    public void joinRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                chatRoom.addClient(clientThread);
                return;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public String removeFromPreviousRoom(String name, ClientThread clientThread) throws ClientNotOwnerException {
        String prevRoomName = "";
        ChatRoom prevChatroom = getChatroomfromClientId(clientThread.getId());
        if (prevChatroom != null) {
            prevChatroom.delete(clientThread.getId());
            prevRoomName = prevChatroom.getRoomId();
        }
        return prevRoomName;
    }

    public void changeRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotOwnerException, ClientAlreadyInChatRoomException {
        String prevRoomName = removeFromPreviousRoom(name, clientThread);
        joinRoom(name, clientThread);
        clientThread.generateResponse(ServerMessage.getMoveJoinRequest(clientThread.getId(), prevRoomName, name));
    }

    public void deleteRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getOwner().equals(clientThread.getId())) {
                chatRoom.addClient(clientThread);
                return;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public ChatRoom getChatroomFromName(String name) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                return chatRoom;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public void deleteChatroom(String name, String clientId)
            throws ChatroomDoesntExistsException, ClientNotOwnerException {
        getChatroomFromName(name).delete(clientId);
    }

    public ChatRoom getChatroomfromClientId(String clientId) {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getOwner().equals(clientId)) {
                return chatRoom;
            }
        }
        return null;
    }

    public void sendMessage(String message, String clientId, String chatroomName)
            throws ChatroomDoesntExistsException, ClientNotInChatRoomException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(chatroomName)) {
                chatRoom.sendMessage(message, clientId);
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public void quit(String chatroomName, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotInChatRoomException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(chatroomName)) {
                chatRoom.removeClient(clientThread);
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public ArrayList<ChatRoom> getChatrooms() {
        return chatrooms;
    }

    public void setChatrooms(ArrayList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }

    public static ChatRoomHandler getChatRoomHandler() {
        return chatRoomHandler;
    }
}
