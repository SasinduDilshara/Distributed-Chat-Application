package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;

import org.json.simple.JSONObject;

import java.net.Socket;
import java.nio.charset.spi.CharsetProvider;
import java.text.ParseException;

public class ClientThread implements Runnable {
    private String id;
    private Socket socket;
    private ChatRoom currentChatRoom;
    private ChatRoomHandler chatRoomHandler;
    private Boolean exit;

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler) {
        this.socket = socket;
        this.chatRoomHandler = chatRoomHandler;
        this.currentChatRoom = chatRoomHandler.getMainChatRoom();
        this.exit = true;
    }

    public void validate() {
        // Get approval from leader
    }

    public void sendMessage() {
        //Class the chatroom send message method
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        while(!exit) {

        }
    }

    public void parse(String message) {

    }

    public void messageReceive(JSONObject message) throws ParseException{
        String type = (String) message.get("type");

        // newidentity
        if (type.equals("newidentity")) {
            String identity = (String) message.get("identity");
            if(ClientHandler.validateClientID(identity)){
                // chatRoomHandler.registerNewUser(this, identity);
                // Add to main hall
            }
            else{
                // destroy this thread
            }
        }
        // list
        else if(type.equals("list")){
            chatRoomHandler.sendChatRoomList(this);
        }
        // who
        else if(type.equals("who")){
            chatRoomHandler.sendChatRoomParticipants(this);
        }
        // createroom
        else if(type.equals("createroom")){
            String roomid = (String) message.get("roomid");
            chatRoomHandler.createChatRoom(roomid, this);
        }
        // joinroom
        else if(type.equals("joinroom")){
            String roomid = (String) message.get("roomid");
            chatRoomHandler.joinRoom(roomid, this);
        }
        // movejoin
        else if(type.equals("movejoin")){
            String former = (String) message.get("former");
            String roomid = (String) message.get("roomid");
            String identity = (String) message.get("identity");
            chatRoomHandler.moveJoinUser(this, former, roomid, identity);
        }
        // deleteroom
        else if(type.equals("deleteroom")){
            String roomid = (String) message.get("roomid");
            chatRoomHandler.deleteRoom(roomid, this);
        }
        // message
        else if(type.equals("message")){
            String content = (String) message.get("content");
            chatRoomHandler.sendMessage(this, content);
        }
        // quit
        else if(type.equals("quit")){
            chatRoomHandler.quit(this);
        }
        else{
            // Ignore unsupported message types
        }
    }


}
