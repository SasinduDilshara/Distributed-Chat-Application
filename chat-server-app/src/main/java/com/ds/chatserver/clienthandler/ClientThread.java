package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.PortUnreachableException;
import java.net.Socket;

public class ClientThread implements Runnable {
    private String id;
    private Socket socket;
    private ChatRoom currentChatRoom;
    private ChatRoomHandler chatRoomHandler;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private Boolean exit;

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler) throws IOException {
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatRoomHandler = chatRoomHandler;
//        this.currentChatRoom = chatRoomHandler.getMainChatRoom();
        this.exit = false;
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

    public void stop() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.exit = true;
    }

    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        String jsonString = null;
        try {
            jsonString = bufferedReader.readLine();
            JSONObject jsonObject = (JSONObject)parser.parse(jsonString);
            String type = (String)jsonObject.get("type");
            if ( type.equals("newidentity")) {
                String identity = (String) jsonObject.get("identity");
                System.out.println(identity);
//                if(ClientHandler.validateClientID(identity)){
//                    // chatRoomHandler.registerNewUser(this, identity);
//                    // Add to main hall
//                }
//                else{
//                    this.stop();
//                }
            } else {
                this.stop();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        while(!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    this.stop();
                    break;
                } else {
                    JSONObject jsonObject = (JSONObject)parser.parse(jsonString);
                    System.out.println(jsonString);
//                    messageReceive(jsonObject);
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void messageReceive(JSONObject message) throws ParseException{
        String type = (String) message.get("type");

        // list
        if(type.equals("list")){
//            chatRoomHandler.sendChatRoomList(this);
        }
        // who
        else if(type.equals("who")){
//            chatRoomHandler.sendChatRoomParticipants(this);
        }
        // createroom
        else if(type.equals("createroom")){
            String roomid = (String) message.get("roomid");
//            chatRoomHandler.createChatRoom(roomid, this);
        }
        // joinroom
        else if(type.equals("joinroom")){
            String roomid = (String) message.get("roomid");
//            chatRoomHandler.joinRoom(roomid, this);
        }
        // movejoin
        else if(type.equals("movejoin")){
            String former = (String) message.get("former");
            String roomid = (String) message.get("roomid");
            String identity = (String) message.get("identity");
//            chatRoomHandler.moveJoinUser(this, former, roomid, identity);
        }
        // deleteroom
        else if(type.equals("deleteroom")){
            String roomid = (String) message.get("roomid");
//            chatRoomHandler.deleteRoom(roomid, this);
        }
        // message
        else if(type.equals("message")){
            String content = (String) message.get("content");
//            chatRoomHandler.sendMessage(this, content);
        }
        // quit
        else if(type.equals("quit")){
//            chatRoomHandler.quit(this);
        }
        else{
            // Ignore unsupported message types
        }
    }


}
