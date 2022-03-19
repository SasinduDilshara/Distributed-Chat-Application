package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.*;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ClientMessage;
import com.ds.chatserver.utils.JsonParser;
import com.ds.chatserver.utils.ServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.*;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

@Slf4j
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

    public ClientThread(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.chatRoomHandler = ChatRoomHandler.getInstance(server.getServerId());
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

    public void stop() {
        try {
            this.socket.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        this.exit = true;
    }

    @Override
    public void run() {
        String jsonString = null;
        try {
            jsonString = bufferedReader.readLine();
            JSONObject request = JsonParser.stringToJSONObject(jsonString);
            String type = (String)request.get(TYPE);

            switch (type) {
                case NEW_IDENTITY -> {
                    handleNewIdentity(request);
                }
                case MOVE_JOIN -> {
                    handleMoveJoin(request);
                }
                default -> this.stop();
            }

        } catch (IOException e) {
            e.printStackTrace();
            this.stop();
        }

        while(!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    log.debug("Abtruply disconnected");
                    manageClientClosure();
                } else {
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                    handleClientRequest(jsonObject);
                }
            } catch (SocketException e) {
                log.debug("Abruptly closed in");
                manageClientClosure();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClientRequest(JSONObject message) {
        String type = (String) message.get(TYPE);

        switch (type) {
            case LIST -> {
                handleList();
            }
            case WHO -> {
                handleWho();
            }
            case CREATE_ROOM -> {
                handleCreateroom(message);
            }
            case JOIN_ROOM -> {
                handleJoinRoom(message);
            }
            case DELETE_ROOM -> {
                handleDeleteRoom(message);
            }
            case MESSAGE -> {
                handleMessage(message);
            }
            case QUIT -> {
                handleQuitRequest(message, true);
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

    private void handleQuitRequest(JSONObject message, boolean clientActive) {
        boolean isOwner = this.equals(this.currentChatRoom.getOwner());
        JSONObject clientResponse = null;
        message.put(IDENTITY, this.id);
        message.put(ROOM_ID, this.currentChatRoom.getRoomId());
        log.info("Quit request - clientId: {},", this.id);
        while(clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        log.info("Quit request - clientId: {} , response: {} approved: true",
                this.id, clientResponse);
        if(clientActive) {
            log.debug("Sending quit response to client");
            this.sendResponse(clientResponse);
        }
        try {
            /**
             * is owner
              */
            if(isOwner) {
                this.chatRoomHandler.deleteRoom(this, true, !clientActive);
            } else {
                log.debug("removing client from the room");
                this.currentChatRoom.removeClient(this, "");
            }
        } catch (ClientNotInChatRoomException | ChatroomDoesntExistsException
                | ClientNotOwnerException | ClientAlreadyInChatRoomException e) {
            e.printStackTrace();
        }
        this.stop();
    }

    private void handleJoinRoom(JSONObject message) {
        log.info("Joinroom request - clientId: {}", this.id);
        message.put(IDENTITY, this.id);
        message.put(FORMER, this.currentChatRoom.getRoomId());
        JSONObject clientResponse = null;
        while(clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        if(clientResponse.get(TYPE).equals(ROOM_CHANGE)
                && !clientResponse.get(ROOM_ID).equals(clientResponse.get(FORMER))) {
//            log.info("Joinroom request - roomchange response - clientId: {} approved: true", this.id);
            try {
                this.currentChatRoom.removeClient(this, (String) clientResponse.get(ROOM_ID));
                this.currentChatRoom = chatRoomHandler.getChatroomFromName((String) clientResponse.get(ROOM_ID));
                this.currentChatRoom.addClient(this, (String) clientResponse.get(FORMER));
            } catch (ClientNotInChatRoomException | ClientAlreadyInChatRoomException | ChatroomDoesntExistsException e) {
                e.printStackTrace();
            }
        } else if(clientResponse.get(TYPE).equals(ROUTE)) {
            log.info("Joinroom request - route response - clientId: {} approved: true, response: {}",
                    this.id,
                    clientResponse);
            try {
                this.currentChatRoom.removeClient(this, (String) clientResponse.get(ROOM_ID));
                reroute = true;
            } catch (ClientNotInChatRoomException e) {
                e.printStackTrace();
            }
        } else {
            log.info("Joinroom - roomchange response - clientId: {} approved: false", this.id);
        }
        this.sendResponse(clientResponse);
    }

    private void manageClientClosure() {
        /**
         * client stops the socket gracefully
         * route
         */
        if(reroute) {
            this.stop();
        }
        /**
         * quit
         */
        else {
            JSONObject jsonObject = ClientMessage.getQuitRequest();
            handleQuitRequest(jsonObject, false);
        }
    }

    private void handleNewIdentity(JSONObject request){
        String clientId = request.get(IDENTITY).toString();
        JSONObject clientResponse = null;
        log.info("New client request - clientId: {},", clientId);

        while(clientResponse == null){
            clientResponse = this.server.getState().respondToClientRequest(request);
        }
        log.info("New client request - clientId: {} approved: {}",
                clientId,
                clientResponse.get(APPROVED));
        this.sendResponse(clientResponse);
        if (Boolean.parseBoolean(clientResponse.get(APPROVED).toString())) {
            try {
                this.setId(clientId);
                this.chatRoomHandler.getMainHall().addClientAndNotify(this, "");
            } catch (ClientAlreadyInChatRoomException e) {
                e.printStackTrace();
            }
        } else {
            stop();
        }
    }

    private void handleMoveJoin(JSONObject request){
        this.setId((String) request.get(IDENTITY));
        JSONObject clientResponse = null;
        while(clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(request);
        }

        String roomId = null;
        if(clientResponse.containsKey(ROOM_ID)){
            roomId = (String) clientResponse.get(ROOM_ID);
            clientResponse.remove(ROOM_ID);
        }
        this.sendResponse(clientResponse);

        log.info("Move Join request - clientId: {} approved: {}",
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
            } catch (ChatroomDoesntExistsException e) {
                e.printStackTrace();
            }
        }
        else {
            stop();
        }
    }

    private void handleList(){
        JSONObject response = ServerMessage
                .getRoomListResponse(SystemState.getChatRooms());
        sendResponse(response);
        log.info("Successfully Send List of ChatRooms to {}", id);
    }

    private void handleWho(){
        ClientThread owner = currentChatRoom.getOwner();
        String ownerName = owner!=null?owner.getId(): "";
        JSONObject response = ServerMessage.getWhoResponse(
                currentChatRoom.getRoomId(),
                currentChatRoom.getClientIds(),
                ownerName);
        sendResponse(response);
        log.info("Successfully Send List of Clients of room {} to {}", currentChatRoom.getRoomId(), id);
    }

    private void handleMessage(JSONObject message) {
        String content = (String) message.get(CONTENT);
        try {
            currentChatRoom.sendMessage(content, this);
        } catch (ClientNotInChatRoomException e) {
            e.printStackTrace();
        }
        log.info("{} send the message : {} to the chat room {}", id, content, currentChatRoom);
    }

    private void handleDeleteRoom(JSONObject message) {
        message.put(IDENTITY, this.getId());
        JSONObject deleteRoomResponse = null;
        JSONObject response = null;
        log.info("Delete Chatroom request - Room Id: {}, Client ID: {}",
                message.get(ROOM_ID).toString(),
                message.get(IDENTITY).toString());
        while(deleteRoomResponse == null) {
            deleteRoomResponse = this.server.getState().respondToClientRequest(message);
        }
        log.info("Delete chatroom request - roomID: {} approved: {}",
                message.get(ROOM_ID).toString(),
                deleteRoomResponse.get(APPROVED));

        for (int i = 0; i < 2; i++) {
            if (!deleteRoomResponse.containsKey(String.valueOf(i))) {
                continue;
            }
            response = (JSONObject) deleteRoomResponse.get(String.valueOf(i));
            if (response.get(TYPE).toString().equals(ROOM_CHANGE)) {
                try {
                    this.chatRoomHandler.deleteRoom(this);
                } catch (ChatroomDoesntExistsException | ClientNotOwnerException | ClientAlreadyInChatRoomException
                        | ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
            }
            sendResponse(response);
        }
    }

    private void handleCreateroom(JSONObject message) {
        JSONObject createRoomResponse = null;
        JSONObject response = null;
        message.put(IDENTITY, this.getId());
        log.info("New Chatroom request - Room Id: {}, Client ID: {}",
                message.get(ROOM_ID).toString(),
                this.getId());
        while(createRoomResponse == null) {
            createRoomResponse = this.server.getState().respondToClientRequest(message);
        }

        for (int i = 0; i < 2; i++) {
            if (!createRoomResponse.containsKey(String.valueOf(i))) {
                continue;
            }
            response = (JSONObject) createRoomResponse.get(String.valueOf(i));
            if (response.get(TYPE).toString().equals(ROOM_CHANGE)) {
                try {
                    this.chatRoomHandler.createChatRoom(message.get(ROOM_ID).toString(), this);
                } catch (ClientNotInChatRoomException e) {
                    e.printStackTrace();
                }
            }
            else {
                log.info("New chatroom request - roomid: {} approved: {}",
                        message.get(ROOM_ID).toString(),
                        response.get(APPROVED));
            }
            sendResponse(response);
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
