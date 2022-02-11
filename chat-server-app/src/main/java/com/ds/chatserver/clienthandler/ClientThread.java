package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.ChatroomDoesntExistsException;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.utils.JsonParser;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.Validation;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;


public class ClientThread implements Runnable {
    private String id;
    private final Socket socket;
    private final ChatRoomHandler chatRoomHandler;
    private ChatRoom currentChatRoom;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Boolean exit;

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler) throws IOException {
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatRoomHandler = chatRoomHandler;
        this.currentChatRoom = chatRoomHandler.getMainHall();
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
        String jsonString = null;
        try {
            jsonString = bufferedReader.readLine();
            JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
            String type = (String)jsonObject.get("type");
            if ( type.equals("newidentity")) {
                String identity = (String) jsonObject.get("identity");
                this.id = identity;
                System.out.println(identity);
                if (Validation.validateClientID(identity)) {
                    try {
                        logger.info("New Client with id {} joined", identity);
                        this.sendResponse(ServerMessage.getNewIdentityResponse(true));
                        chatRoomHandler.joinRoom(
                                chatRoomHandler.getMainHall().getRoomId(), this, "");
                    } catch (ChatroomDoesntExistsException e) {
                        e.printStackTrace();
                    } catch (ClientAlreadyInChatRoomException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    this.stop();
                }
            } else if  (type.equals("movejoin")) {
                //TODO: complete
            } else {
                this.stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    this.stop();
                    break;
                } else {
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);;
                    System.out.println(jsonString);
//                    messageReceive(jsonObject);
                }
            } catch (IOException e) {
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

    public void sendResponse(JSONObject returnMessage) {
//        this.printWriter.print(returnMessage.toJSONString());
        try {
            DataOutputStream dout =new DataOutputStream(this.socket.getOutputStream());
            dout.write((returnMessage.toJSONString() + "\n").getBytes("UTF-8"));
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
