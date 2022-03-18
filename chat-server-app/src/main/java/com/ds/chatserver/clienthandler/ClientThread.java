package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ClientMessage;
import com.ds.chatserver.utils.JsonParser;
import com.ds.chatserver.utils.ServerMessage;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

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
    private boolean exit;
    private Boolean reroute;

    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public ClientThread(Socket socket, ChatRoomHandler chatRoomHandler, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatRoomHandler = chatRoomHandler;
        this.currentChatRoom = chatRoomHandler.getMainHall();
        this.exit = false;
        this.reroute = false;
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
                            ChatRoomHandler.getInstance(server.getServerId()).getMainHall().addClientAndNotify(this, "");
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
                case MOVE_JOIN -> {
                    this.setId((String) request.get(IDENTITY));
                    while(clientResponse == null) {
                        clientResponse = this.server.getState().respondToClientRequest(request);
                    }

                    String roomId = null;
                    if(clientResponse.containsKey(ROOM_ID)){
                        roomId = (String) clientResponse.get(ROOM_ID);
                        clientResponse.remove(ROOM_ID);
                    }
                    this.sendResponse(clientResponse);

                    logger.info("Move Join request - clientId: {} approved: {}",
                            request.get(IDENTITY).toString(),
                            clientResponse.get(APPROVED));

                    if (Boolean.parseBoolean((String) clientResponse.get(APPROVED))) {
                        try {
                            String prevRoomId = (String) request.get(FORMER);
                            this.currentChatRoom = chatRoomHandler.getChatroomFromName(roomId);
                            try {
                                this.currentChatRoom.addClientAndNotify(this, prevRoomId);
                            } catch (ClientAlreadyInChatRoomException e) {
                                e.printStackTrace();
                            }
//                                clientResponse.remove(ROOM_ID);
                        } catch (ChatroomDoesntExistsException e) {
                            e.printStackTrace();
                        }
                    }
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
                    logger.debug("Abtruply disconnected");
                    manageClientClosure();
                } else {
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                    handleClientRequest(jsonObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClientRequest(JSONObject message) {
        String type = (String) message.get("type");

        switch (type) {
            case LIST -> {
                JSONObject response = ServerMessage
                        .getRoomListResponse(SystemState.getChatRooms());
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
                JSONObject createRoomResponse = null, response = null;
                message.put(IDENTITY, this.getId());
                logger.info("New Chatroom request - Room Id: {},", message.get(ROOM_ID).toString(), "Client ID:- ",
                       this.getId());
                while(createRoomResponse == null){
                    createRoomResponse = this.server.getState().respondToClientRequest(message);
                }
                logger.info("New chatroom request - roomid: {} approved: {}",
                        message.get(ROOM_ID).toString(),
                        createRoomResponse.get(APPROVED));

                for (int i = 0; i < 2; i++) {
                    if (!createRoomResponse.containsKey(String.valueOf(i))) {
                        continue;
                    }
                    response = (JSONObject) createRoomResponse.get(String.valueOf(i));
                    if (response.get(TYPE).toString().equals(ROOM_CHANGE)) {
                        try {
                            ChatRoomHandler.getInstance(server.getServerId()).createChatRoom(message.get(ROOM_ID).toString(), this);
                        } catch (ClientNotInChatRoomException e) {
                            e.printStackTrace();
                        }
                    }
                    sendResponse(response);
                }
            }
            case JOIN_ROOM -> handleJoinRoomRequest(message);
            case DELETE_ROOM -> {
                message.put(IDENTITY, this.getId());
                JSONObject deleteRoomResponse = null, response = null;
                logger.info("Delete Chatroom request - Room Id: {},", message.get(ROOM_ID).toString(), "Client ID:- ",
                        message.get(IDENTITY).toString());
                while(deleteRoomResponse == null){
                    deleteRoomResponse = this.server.getState().respondToClientRequest(message);
                }
                logger.info("Delete chatroom request - roomID: {} approved: {}",
                        message.get(ROOM_ID).toString(),
                        deleteRoomResponse.get(APPROVED));

                for (int i = 0; i < 2; i++) {
                    if (!deleteRoomResponse.containsKey(String.valueOf(i))) {
                        continue;
                    }
                    response = (JSONObject) deleteRoomResponse.get(String.valueOf(i));
                    if (response.get(TYPE).toString().equals(ROOM_CHANGE)) {
                        try {
                            ChatRoomHandler.getInstance(server.getServerId()).deleteRoom(this);
                        } catch (ChatroomDoesntExistsException | ClientNotOwnerException | ClientAlreadyInChatRoomException
                                | ClientNotInChatRoomException e) {
                            e.printStackTrace();
                        }
                    }
                    sendResponse(response);
                }
            }
            case MESSAGE -> {
                String content = (String) message.get("content");
                try {
                    currentChatRoom.sendMessage(content, this);
                } catch (ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
                logger.info("{} send the message : {} to the chat room {}", id, content, currentChatRoom);
            }
            case QUIT -> handleQuitRequest(message, true);
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

    private void handleQuitRequest(JSONObject message, boolean clientActive) {
        boolean isOwner = this.equals(this.currentChatRoom.getOwner());
        JSONObject clientResponse = null;
        message.put(IDENTITY, this.id);
        message.put(ROOM_ID, this.currentChatRoom.getRoomId());
        logger.info("Quit request - clientId: {},", this.id);
        while(clientResponse == null){
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        logger.info("Quit request - clientId: {} , response: {} approved: true",
                this.id, clientResponse);
        if(clientActive) {
            logger.debug("Sending quit response to client");
            this.sendResponse(clientResponse);
        }
        try {
            // is owner
            if(isOwner) {
                ChatRoomHandler.getInstance(server.getServerId()).deleteRoom(this, true, !clientActive);
            } else {
                logger.debug("removing client from the room");
                this.currentChatRoom.removeClient(this, "");
            }
        } catch (ClientNotInChatRoomException | ChatroomDoesntExistsException
                | ClientNotOwnerException | ClientAlreadyInChatRoomException e) {
            e.printStackTrace();
        }
        this.stop();
    }

    private void handleJoinRoomRequest(JSONObject message) {
        logger.info("Joinroom request - clientId: {}", this.id);
        message.put(IDENTITY, this.id);
        message.put(FORMER, this.currentChatRoom.getRoomId());
        JSONObject clientResponse = null;
        while(clientResponse==null) {
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        if(clientResponse.get(TYPE).equals(ROOM_CHANGE)
                && !clientResponse.get(ROOM_ID).equals(clientResponse.get(FORMER))) {
            logger.info("Joinroom request - roomchange response - clientId: {} approved: true", this.id);
            try {
                this.currentChatRoom.removeClient(this, (String) clientResponse.get(ROOM_ID));
                this.currentChatRoom = chatRoomHandler.getChatroomFromName((String) clientResponse.get(ROOM_ID));
                this.currentChatRoom.addClient(this, (String) clientResponse.get(FORMER));
            } catch (ClientNotInChatRoomException | ClientAlreadyInChatRoomException | ChatroomDoesntExistsException e) {
                e.printStackTrace();
            }
        } else if(clientResponse.get(TYPE).equals(ROUTE)){
            logger.info("Joinroom request - route response - clientId: {} approved: true, response: {}",
                    this.id, clientResponse);
            try {
                this.currentChatRoom.removeClient(this, (String) clientResponse.get(ROOM_ID));
                reroute = true;
            } catch (ClientNotInChatRoomException e) {
                e.printStackTrace();
            }
            //TODO: close this thread.
        } else {
            logger.info("Joinroom - roomchange response - clientId: {} approved: false", this.id);
        }
        this.sendResponse(clientResponse);
    }

    private void manageClientClosure() {
        // client stops the socket gracefully
        // route
        if(reroute) {
            this.stop();
        } else {
            // quit
            JSONObject jsonObject = ClientMessage.getQuitRequest();
            handleQuitRequest(jsonObject, false);
        }
    }

    @Override
    public String toString() {
        return "ClientThread{" +
                "id='" + id + '\'' +
                ", server=" + server.getServerId() +
                '}';
    }
}
