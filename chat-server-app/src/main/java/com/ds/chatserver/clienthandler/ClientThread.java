package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.systemstate.ChatroomLog;
import com.ds.chatserver.utils.JsonParser;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.Validation;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.*;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;


public class ClientThread implements Runnable {
    private String id;
    private final Socket socket;
    private Server server;
    private final ChatRoomHandler chatRoomHandler;
    private ChatRoom currentChatRoom;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;
    private Boolean exit;

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatRoomHandler = chatRoomHandler;
        this.currentChatRoom = chatRoomHandler.getMainHall();
        this.exit = false;
    }

    public String getId() {
        return id;
    }

    public ChatRoom getCurrentChatRoom() {
        return this.currentChatRoom;
    }

    public void setCurrentChatRoom(ChatRoom currentChatRoom) {
        this.currentChatRoom = currentChatRoom;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void validate() {
        // Get approval from leader
    }

    public void sendMessage() {
        //Class the chatroom send message method
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
            JSONObject request = JsonParser.stringToJSONObject(jsonString);
            String type = (String)request.get("type");
            JSONObject clientResponse = null;
            logger.debug(jsonString);

            switch (type) {
                case "newidentity" -> {
                    String clientId = request.get(IDENTITY).toString();
                    logger.info("New client request - clientId: {},", clientId);
                    while(clientResponse == null){
                        clientResponse = this.server.getState().respondToClientRequest(request);
//                        logger.debug(clientResponse.toString());
                    }
                    logger.info("New client request - clientId: {} approved: {}",
                            clientId,
                            clientResponse.get(APPROVED));
                    this.setId(clientId);
                    this.sendResponse(clientResponse);
                    if (Boolean.parseBoolean(clientResponse.get(APPROVED).toString())) {
                        try {
                            ChatRoomHandler.getInstance(server.getServerId()).getMainHall().addClient(this, "");
                        } catch (ClientAlreadyInChatRoomException e) {
                            e.printStackTrace();
                        }
                    }
//                    String identity = (String) jsonObject.get("identity");
//                    this.id = identity;
//                    JSONObject serverResponse = this.server.handleClientRequest(jsonObject);
//                    if (Validation.validateClientID(identity)) {
//                        try {
//                            this.sendResponse(ServerMessage.getNewIdentityResponse(true));
//                            chatRoomHandler.joinRoom(
//                                    chatRoomHandler.getMainHall().getRoomId(), this, null);
//                            logger.info("New Client with id {} joined", identity);
//                        } catch (ChatroomDoesntExistsException | ClientAlreadyInChatRoomException |
//                                ClientNotInChatRoomException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        this.sendResponse(ServerMessage.getNewIdentityResponse(false));
//                        logger.info("New Client join request with id {} failed", identity);
//                        // TODO: check if directly stopping the thread is okay
//                        this.stop();
//                    }
                }
                case "movejoin" -> {
//                    String former = (String) jsonObject.get("former");
//                    String roomId = (String) jsonObject.get("roomid");
//                    this.id = (String) jsonObject.get("identity");
//                    chatRoomHandler.moveJoinRoom(roomId, this, former);
//                    //TODO: serverId
//                    sendResponse(ServerMessage.getServerChangeResponse("serverId"));
//                    logger.info("Successfully changed server of {}", id);
                }
                default -> this.stop();
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
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                    handleClientRequest(jsonObject);
                }
            } catch (IOException e) {
//                TODO implement quit
                e.printStackTrace();
            }
        }
    }

    public void handleClientRequest(JSONObject message) {
        String type = (String) message.get("type");

        switch (type) {
            case LIST -> {
                JSONObject response = ServerMessage.getRoomListResponse(getRoomIdFromChatRooms());
                sendResponse(response);
                logger.info("Successfully Send List of ChatRooms to {}", id);
            }
            case WHO -> {
                ClientThread owner = currentChatRoom.getOwner();
                String ownerName = owner!=null?owner.getId(): "";
                JSONObject response = ServerMessage.getWhoResponse(
                        currentChatRoom.getRoomId(),
                        currentChatRoom.getClientIds(),
                        ownerName);
                sendResponse(response);
                logger.info("Successfully Send List of Clients of room {} to {}", currentChatRoom.getRoomId(), id);
            }
            case CREATE_ROOM -> {
                JSONObject createRoomResponse = null;
                message.put(IDENTITY, this.getId());
                logger.info("New Chatroom request - Room Id: {},", message.get(ROOM_ID_2).toString(), "Client ID:- ",
                       this.getId());
                while(createRoomResponse == null){
                    createRoomResponse = this.server.getState().respondToClientRequest(message);
                }
                logger.info("New chatroom request - roomid: {} approved: {}",
                        message.get(ROOM_ID_2).toString(),
                        createRoomResponse.get(APPROVED));
                try {
                    ChatRoomHandler.getInstance(server.getServerId()).createChatRoom(message.get(ROOM_ID_2).toString(), this);
                } catch (ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
                sendResponse(createRoomResponse);
            }
            case JOIN_ROOM -> {
                String roomId = (String) message.get("roomid");
                try {
                    chatRoomHandler.joinRoom(roomId, this, currentChatRoom);
                } catch (ChatroomDoesntExistsException | ClientAlreadyInChatRoomException |
                        ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
            }
            case DELETE_ROOM -> {
                message.put(IDENTITY, this.getId());
                JSONObject deleteRoomResponse = null;
                logger.info("Delete Chatroom request - Room Id: {},", message.get(ROOM_ID_2).toString(), "Client ID:- ",
                        message.get(IDENTITY).toString());
                while(deleteRoomResponse == null){
                    deleteRoomResponse = this.server.getState().respondToClientRequest(message);
                }
                logger.info("Delete chatroom request - roomID: {} approved: {}",
                        message.get(ROOM_ID_2).toString(),
                        deleteRoomResponse.get(APPROVED));
                try {
                    ChatRoomHandler.getInstance(server.getServerId()).deleteRoom(this);
                } catch (ChatroomDoesntExistsException e) {
                    e.printStackTrace();
                } catch (ClientNotOwnerException e) {
                    e.printStackTrace();
                } catch (ClientAlreadyInChatRoomException e) {
                    e.printStackTrace();
                } catch (ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
                sendResponse(deleteRoomResponse);
            }
            case MESSAGE -> {
                String content = (String) message.get("content");
                try {
                    currentChatRoom.sendMessage(content, this.id);
                } catch (ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
                logger.info("{} send the message : {} to the chat room {}", id, content, currentChatRoom);
            }
            case QUIT -> {
                try {
                    chatRoomHandler.quit(this);
                } catch (ChatroomDoesntExistsException | ClientAlreadyInChatRoomException |
                        ClientNotInChatRoomException | ClientNotOwnerException e) {
                    e.printStackTrace();
                }
            }
            default -> {
                // Ignore unsupported message types
            }
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

    @Override
    public String toString() {
        return "ClientThread{" +
                "id='" + id + '\'' +
                ", server=" + server.getServerId() +
                '}';
    }
}
