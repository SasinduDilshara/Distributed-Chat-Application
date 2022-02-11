package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.utils.JsonParser;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.Validation;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


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
    public ChatRoom getCurrentChatRoom() {
        return this.currentChatRoom;
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
                if (Validation.validateClientID(identity)) {
                    try {
                        logger.info("New Client with id {} joined", identity);
                        this.sendResponse(ServerMessage.getNewIdentityResponse(true));
                        chatRoomHandler.joinRoom(
                                chatRoomHandler.getMainHall().getRoomId(), this, null);
                    } catch (ChatroomDoesntExistsException e) {
                        e.printStackTrace();
                    } catch (ClientAlreadyInChatRoomException | ClientNotInChatRoomException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    this.stop();
                }
            } else if  (type.equals("movejoin")) {
                //TODO: complete
                String former = (String) jsonObject.get("former");
                String roomId = (String) jsonObject.get("roomid");
                String identity = (String) jsonObject.get("identity");
                this.id = identity;
                //TODO:
//                chatRoomHandler.joinRoom(
//                        roomId, this, former);
                //TODO: serverId
                sendResponse(ServerMessage.getServerChangeResponse("serverId"));
                logger.info("Successfully Send List of ChatRooms to {}", id);
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
                    handleClientRequest(jsonObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClientRequest(JSONObject message) {
        String type = (String) message.get("type");

        if(type.equals("list")){
            JSONObject response = ServerMessage.
                    getRoomListResponse(
                            getRoomIdFromChatRooms());
            sendResponse(response);
            logger.info("Successfully Send List of ChatRooms to {}", id);
        }

        else if(type.equals("who")){
            JSONObject response = ServerMessage.getWhoResponse(
                    currentChatRoom.getRoomId(),
                    currentChatRoom.getClientIds(),
                    currentChatRoom.getOwner().getId());
            sendResponse(response);
            logger.info("Successfully Send List of Clients to {} of {}", id, currentChatRoom.getRoomId());
        }

        else if(type.equals("createroom")){
            String roomId = (String) message.get("roomid");
            try {
                chatRoomHandler.createChatRoom(roomId, this);
            } catch (ChatroomAlreadyExistsException e) {
                e.printStackTrace();
            } catch (InvalidChatroomException | ClientNotInChatRoomException e) {
                e.printStackTrace();
            }
//            sendResponse(ServerMessage.getCreateRoomResponse(roomId, true));
            logger.info("Successfully created chat room {}", roomId);
        }

        // joinroom
        else if(type.equals("joinroom")){
            String roomId = (String) message.get("roomid");
            try {
                chatRoomHandler.joinRoom(roomId, this, currentChatRoom);
            } catch (ChatroomDoesntExistsException e) {
                e.printStackTrace();
            } catch (ClientAlreadyInChatRoomException
                    | ClientNotInChatRoomException e) {
                e.printStackTrace();
            }
        }

        // movejoin
//        else if(type.equals("movejoin")){
//
////            chatRoomHandler.moveJoinUser(this, former, roomId, identity);
//        }

        else if(type.equals("deleteroom")){
            String roomId = (String) message.get("roomid");
            try {
                Boolean success = chatRoomHandler.deleteRoom(roomId, this);
                sendResponse(ServerMessage.getDeleteRoomResponse(roomId, success));

            } catch (ChatroomDoesntExistsException e) {
                e.printStackTrace();
            } catch (ClientNotOwnerException e) {
                e.printStackTrace();
            }
        }

        else if(type.equals("message")){
            String content = (String) message.get("content");
            try {
                currentChatRoom.sendMessage(content, this.id);
            } catch (ClientNotInChatRoomException e) {
                e.printStackTrace();
            }
            logger.info("{} send the message : {} to the chat room {}", id, content, currentChatRoom);
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

    private ArrayList<String> getRoomIdFromChatRooms() {
        ArrayList<String> chatRoomIds = new ArrayList<>();
        for (ChatRoom chatRoom: chatRoomHandler.getChatRooms()) {
            chatRoomIds.add(chatRoom.getRoomId());
        }
        return chatRoomIds;
    }

}
