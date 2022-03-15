package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.utils.Validation;
import com.ds.chatserver.utils.ServerMessage;

import java.util.ArrayList;
import java.util.Objects;

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
        return Validation.validateRoomID(name);
    }

    public Boolean validateChatRoom(String name) {
        //TODO Check??
            return true;
    }

    // create new chat room by user
    public void createChatRoom(String name, ClientThread clientThread)
            throws ChatroomAlreadyExistsException, InvalidChatroomException, ClientNotInChatRoomException {
        if (!(validateChatRoom(name))) {
            throw new ChatroomAlreadyExistsException(name);
        } else if (!(validateChatroomName(name))) {
            throw new InvalidChatroomException(name);
        }
        // remove from prev chatroom
        ChatRoom previousChatRoom = clientThread.getCurrentChatRoom();
        previousChatRoom.removeClient(clientThread, name);
        // create and add to new chatroom
        ChatRoom newChatRoom = ChatRoom.createChatRoom(name, clientThread);
        chatrooms.add(newChatRoom);
        clientThread.setCurrentChatRoom(newChatRoom);
    }

    public void joinRoom(String newRoomName, ClientThread clientThread, ChatRoom prevRoom)
            throws ClientAlreadyInChatRoomException, ClientNotInChatRoomException, ChatroomDoesntExistsException {
        String prevRoomName = (prevRoom!=null)? prevRoom.getRoomId(): "";
        if(prevRoom != null && prevRoom.getOwner().equals(clientThread)) {
            // owner of prev group joining another group - failed joinroom request
            clientThread.sendResponse(ServerMessage.getRoomChangeResponse(
                    clientThread.getId(), prevRoomName, prevRoomName));
        } else if(Objects.equals(newRoomName, "")) {
            // quit request
            if (prevRoom != null) {
                prevRoom.removeClient(clientThread, newRoomName);
            }
            clientThread.sendResponse(ServerMessage.getRoomChangeResponse(
                    clientThread.getId(), prevRoomName, newRoomName));
        } else if(isChatroomInServer(newRoomName)) {
            // if chat room is in this server
            ChatRoom newRoom = getChatroomFromName(newRoomName);
            if (prevRoom != null) {
                prevRoom.removeClient(clientThread, newRoomName);
            }
            newRoom.addClient(clientThread, prevRoomName);
        } else if(Validation.isChatroomInSystem(newRoomName)) {
            // if chat room is in another server
            // TODO: get real host , port values of the server that the room exists
            // remove from the room of this server
            if (prevRoom != null) {
                prevRoom.removeClient(clientThread, newRoomName);
            }
            clientThread.sendResponse(ServerMessage.getRouteResponse(
                    clientThread.getId(), "122.134.2.4", "4445"));
        } else {
            // failed joinroom request
            clientThread.sendResponse(ServerMessage.getRoomChangeResponse(
                    clientThread.getId(), prevRoomName, prevRoomName));
        }
    }

    public void moveJoinRoom(String newRoomName, ClientThread clientThread, String prevRoomName)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException {
        // call join if the room exists, call join for mainhall if it doesnt
        if (isChatroomInServer(newRoomName)) {
            ChatRoom newRoom = getChatroomFromName(newRoomName);
            newRoom.addClient(clientThread, prevRoomName);
        } else {
            mainHall.addClient(clientThread, prevRoomName);
        }

    }


//    public String removeFromPreviousRoom(String newRoomName, ClientThread clientThread)
//            throws ClientNotInChatRoomException {
//        String prevRoomName = "";
//        ChatRoom prevChatroom = getChatroomfromClientId(clientThread.getId());
//        if (prevChatroom != null) {
//            if (prevChatroom.getOwner().getId().equals(clientThread.getId())) {
//            } else {
//                prevChatroom.removeClient(clientThread, newRoomName);
//                prevRoomName = prevChatroom.getRoomId();
//            }
//        }
//        return prevRoomName;
//    }

//    private void changeRoom(String newRoomName, ClientThread clientThread, Boolean quit)
//            throws ChatroomDoesntExistsException,
//            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
//        String prevRoomName = removeFromPreviousRoom(newRoomName, clientThread);
//        if (!quit){
//            //TODO: bug fix needed.
////            joinRoom(newRoomName, clientThread, prevRoomName);
//        }
//    }

//    public void changeRoom(String newRoomName, ClientThread clientThread)
//            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
//        changeRoom(newRoomName, clientThread, false);
//    }

    // remove the users from the room when quiting
    public void quit(ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientAlreadyInChatRoomException,
            ClientNotInChatRoomException, ClientNotOwnerException {
        //TODO: remove the other users if the quiting user is the owner
        if(clientThread.equals(clientThread.getCurrentChatRoom().getOwner())) {
            deleteRoom(clientThread);
        }
        joinRoom("", clientThread, clientThread.getCurrentChatRoom());
    }

    public Boolean deleteRoom(ClientThread clientThread)
            throws ChatroomDoesntExistsException, ClientNotOwnerException,
            ClientAlreadyInChatRoomException, ClientNotInChatRoomException {
        // TODO: if success=true inform other servers and delete the room instance.
        ChatRoom chatRoom = clientThread.getCurrentChatRoom();
        if (clientThread.equals(chatRoom.getOwner())) {

            for(ClientThread client: chatRoom.getClients()) {
//                chatRoom.getClients().remove(client);
                client.sendResponse(ServerMessage.getRoomChangeResponse(
                        client.getId(), chatRoom.getRoomId(), mainHall.getRoomId()));
                client.setCurrentChatRoom(mainHall);
            }
            mainHall.addClients(chatRoom.getClients(), chatRoom.getRoomId());
            // all pointers to chatroom deleted
            chatrooms.remove(chatRoom);
            clientThread.setCurrentChatRoom(mainHall);
            return true;
        }
        return false;
    }

    public boolean isChatroomInServer(String name) {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ChatRoom getChatroomFromName(String name) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getRoomId().equals(name)) {
                return chatRoom;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }
//
//    public ChatRoom getChatroomfromClientId(String clientId) {
//        for (ChatRoom chatRoom: chatrooms) {
//            if (chatRoom.getOwner().getId().equals(clientId)) {
//                return chatRoom;
//            }
//        }
//        return null;
//    }

//    public void sendMessage(String message, String clientId, String chatroomName)
//            throws ChatroomDoesntExistsException, ClientNotInChatRoomException {
//        for (ChatRoom chatRoom: chatrooms) {
//            if (chatRoom.getRoomId().equals(chatroomName)) {
//                chatRoom.sendMessage(message, clientId);
//            }
//        }
//        throw new ChatroomDoesntExistsException(chatroomName);
//    }

//    public void removeClient(String chatroomName, ClientThread clientThread)
//            throws ChatroomDoesntExistsException, ClientNotInChatRoomException, ClientNotOwnerException {
//        for (ChatRoom chatRoom: chatrooms) {
//            if (chatRoom.getRoomId().equals(chatroomName)) {
//                if (chatRoom.getOwner().getId().equals(clientThread.getId())) {
//                    deleteRoom(chatroomName, clientThread);
//                } else {
//                    chatRoom.removeClient(clientThread, "");
//                }
//            }
//        }
//        throw new ChatroomDoesntExistsException(chatroomName);
//    }

    public ArrayList<ChatRoom> getChatRooms() {
        return chatrooms;
    }


    public void setChatrooms(ArrayList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }
}
