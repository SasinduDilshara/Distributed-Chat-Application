package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ChatroomDoesntExistsException;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotOwnerException;
import com.ds.chatserver.utils.ServerMessage;

import java.util.ArrayList;

public class ChatRoomHandler {
    private final ChatRoom mainHall;
    private ArrayList<ChatRoom> chatrooms;
    private static ChatRoomHandler chatRoomHandler;
    private static Object mutex = new Object();

    public static ChatRoomHandler getInstance(String serverId) {
        ChatRoomHandler tempChatRoomHandler = chatRoomHandler;
        if (tempChatRoomHandler == null) {
            synchronized (mutex) {
                tempChatRoomHandler = chatRoomHandler;
                if (tempChatRoomHandler == null) {
                    chatRoomHandler = new ChatRoomHandler(serverId);
                    tempChatRoomHandler = chatRoomHandler;
                }
            }
        }
        return chatRoomHandler;
    }

    private ChatRoomHandler(String serverId) {
        this.mainHall = ChatRoom.createMainHall(serverId);
        this.chatrooms = new ArrayList<>();
        this.chatrooms.add(mainHall);
    }

    public ChatRoom getMainHall() {
        return this.mainHall;
    }

    public void createChatRoom(String name, ClientThread clientThread)
            throws ClientNotInChatRoomException {
        // remove from prev chatroom
        ChatRoom previousChatRoom = clientThread.getCurrentChatRoom();
        previousChatRoom.removeClient(clientThread, name);
        // create and add to new chatroom
        ChatRoom newChatRoom = ChatRoom.createChatRoom(name, clientThread);
        chatrooms.add(newChatRoom);
        clientThread.setCurrentChatRoom(newChatRoom);
    }

    public Boolean deleteRoom(ClientThread clientThread, Boolean isQuit, Boolean isForceQuit)
            throws ChatroomDoesntExistsException, ClientNotOwnerException,
            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        ChatRoom chatRoom = clientThread.getCurrentChatRoom();
        if (clientThread.equals(chatRoom.getOwner())) {
            for(ClientThread client: chatRoom.getClients()) {
                if (client.equals(chatRoom.getOwner())) {
                    if (isQuit) {
                        if (!isForceQuit) {
                            client.sendResponse(ServerMessage.getRoomChangeResponse(
                                    client.getId(), chatRoom.getRoomId(), ""));
                        }
                    } else {
                        client.setCurrentChatRoom(mainHall);
                    }
                } else {
                    client.sendResponse(ServerMessage.getRoomChangeResponse(
                            client.getId(), chatRoom.getRoomId(), mainHall.getRoomId()));
                    client.setCurrentChatRoom(mainHall);
                }
            }
            if (isQuit && clientThread.equals(chatRoom.getOwner())) {
                chatRoom.removeClient(chatRoom.getOwner());
            }
            mainHall.addClients(chatRoom.getClients(), chatRoom.getRoomId());
            // all pointers to chatroom deleted
            chatrooms.remove(chatRoom);
            clientThread.setCurrentChatRoom(mainHall);
            return true;
        }
        return false;
    }

    public Boolean deleteRoom(ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotOwnerException,
            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        return deleteRoom(clientThread, false, false);
    }

    public ChatRoom getChatroomFromName(String name) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                return chatRoom;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }
}
