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
            getChatrooms().add(ChatRoom.createChatRoom(name, clientThread));
            return true;
        }
    }

    public void joinRoom(String name, ClientThread clientThread, String prevRoomName)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                chatRoom.addClient(clientThread, prevRoomName);
                return;
            }
        }
    }

    public String removeFromPreviousRoom(String name, ClientThread clientThread)
            throws ClientNotInChatRoomException {
        String prevRoomName = "";
        ChatRoom prevChatroom = getChatroomfromClientId(clientThread.getId());
        if (prevChatroom != null) {
            if (prevChatroom.getOwner().equals(clientThread.getId())) {
            } else {
                    prevChatroom.removeClient(clientThread);
                    prevRoomName = prevChatroom.getRoomId();
                }
            }
        return prevRoomName;
    }

    public void changeRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException,
                   ClientNotOwnerException, ClientNotInChatRoomException {
        changeRoom(name, clientThread, false);
    }

    public void changeRoom(String name, ClientThread clientThread, Boolean quit)
            throws ChatroomDoesntExistsException, ClientNotOwnerException,
            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        String prevRoomName = removeFromPreviousRoom(name, clientThread);
        if (!quit){
            joinRoom(name, clientThread, prevRoomName);
        }
    }

    public void deleteRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotOwnerException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getOwner().equals(clientThread.getId())) {
                chatRoom.deleteRoom(clientThread.getId());
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

    public void removeClient(String chatroomName, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotInChatRoomException, ClientNotOwnerException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(chatroomName)) {
                if (chatRoom.getOwner().equals(clientThread.getId())) {
                    deleteRoom(chatroomName, clientThread);
                } else {
                    chatRoom.removeClient(clientThread);
                }
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public void quit(ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException,
                   ClientNotOwnerException, ClientNotInChatRoomException {
        changeRoom(getChatroomfromClientId(clientThread.getId()).getRoomId(), clientThread, true);
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
