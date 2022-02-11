package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.*;

import java.util.ArrayList;

public class ChatRoomHandler {
    private final ChatRoom mainHall;
    private ArrayList<ChatRoom> chatrooms;
    private static final ChatRoomHandler chatRoomHandler = getInstance();

    public static ChatRoomHandler getInstance() {
        return new ChatRoomHandler();
    }

    private ChatRoomHandler() {
        this.mainHall = ChatRoom.createMainHall();
        this.chatrooms = new ArrayList<>();
        this.chatrooms.add(mainHall);
    }


    public static ChatRoomHandler getChatRoomHandler() {
        return chatRoomHandler;
    }

    public ChatRoom getMainHall() {
        return this.mainHall;
    }

    private Boolean validateChatroomName(String name) {
        //TODO Implement
        return true;
    }

    public Boolean validateChatRoom(String name) {
//        return (Server.validateChatroom(name));
        return true;
    }

    // create new chat room by user
    public Boolean createChatRoom(String name, ClientThread clientThread)
            throws ChatroomAlreadyExistsException, InvalidChatroomException, ClientNotInChatRoomException {
        if (!(validateChatRoom(name))) {
            throw new ChatroomAlreadyExistsException(name);
        } else if (!(validateChatroomName(name))) {
            throw new InvalidChatroomException(name);
        } else {
            //why ?
            chatrooms.add(ChatRoom.createChatRoom(name, clientThread));
            ChatRoom previousChatRoom = clientThread.getCurrentChatRoom();
            previousChatRoom.removeClient(clientThread, name);
            return true;
        }
    }

    // prev - String
    public void joinRoom(String newRoomName, ClientThread clientThread, ChatRoom prevRoom)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        // if newRoomName is invalid.
        //TODO: handle for multiple server
        // getChatroomFromName(prevRoomName)
        prevRoom.removeClient(clientThread, newRoomName);
        if (prevRoom == null) {
            getChatroomFromName(newRoomName).addClient(clientThread, "");
        } else {
            getChatroomFromName(newRoomName).addClient(clientThread, prevRoom.getRoomId());
        }
//        for (ChatRoom chatRoom: chatrooms) {
//            if (chatRoom.getRoomId().equals(newRoomName)) {
//                chatRoom.addClient(clientThread, prevRoomName);
//                return ;
//            }
//        }

    }

    public String removeFromPreviousRoom(String newRoomName, ClientThread clientThread)
            throws ClientNotInChatRoomException {
        String prevRoomName = "";
        ChatRoom prevChatroom = getChatroomfromClientId(clientThread.getId());
        if (prevChatroom != null) {
            if (prevChatroom.getOwner().getId().equals(clientThread.getId())) {
            } else {
                prevChatroom.removeClient(clientThread, newRoomName);
                prevRoomName = prevChatroom.getRoomId();
            }
        }
        return prevRoomName;
    }

    private void changeRoom(String newRoomName, ClientThread clientThread, Boolean quit)
            throws ChatroomDoesntExistsException,
            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        String prevRoomName = removeFromPreviousRoom(newRoomName, clientThread);
        if (!quit){
            //TODO: bug fix needed.
//            joinRoom(newRoomName, clientThread, prevRoomName);
        }
    }

    public void changeRoom(String newRoomName, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        changeRoom(newRoomName, clientThread, false);
    }

    // remove the users from the room when quiting
    //TODO: remove the other users if the quiting user is the owner
    public void quit(ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        changeRoom(getChatroomfromClientId(clientThread.getId()).getRoomId(), clientThread, true);
    }

    public Boolean deleteRoom(String name, ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotOwnerException {
//        for (ChatRoom chatRoom: chatrooms) {
//            if (chatRoom.getOwner().getId().equals(clientThread.getId())) {
//                chatRoom.deleteRoom(clientThread.getId(), this.mainHall.getRoomId());
//                return;
//            }
//        }
        ChatRoom chatRoom = clientThread.getCurrentChatRoom();
        if (chatRoom.getOwner().getId().equals(clientThread.getId())) {
            chatRoom.deleteRoom(clientThread.getId(), this.mainHall.getRoomId());
            return true;
        } else {
            return false;
        }

//        throw new ChatroomDoesntExistsException(name);
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
            if (chatRoom.getOwner().getId().equals(clientId)) {
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
                if (chatRoom.getOwner().getId().equals(clientThread.getId())) {
                    deleteRoom(chatroomName, clientThread);
                } else {
                    chatRoom.removeClient(clientThread, "");
                }
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public ArrayList<ChatRoom> getChatRooms() {
        return chatrooms;
    }



    public void setChatrooms(ArrayList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }
}
