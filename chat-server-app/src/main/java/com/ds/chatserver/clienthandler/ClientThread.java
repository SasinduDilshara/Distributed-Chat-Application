package com.ds.chatserver.clienthandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.exceptions.ChatroomDoesntExistsException;
import com.ds.chatserver.exceptions.ClientAlreadyInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.ClientNotOwnerException;
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
        }
        this.exit = true;
    }

    @Override
    public void run() {
        String jsonString = null;
        try {
            jsonString = bufferedReader.readLine();
            JSONObject request = JsonParser.stringToJSONObject(jsonString);
            String type = (String) request.get(TYPE);
            String clientId = (String) request.get(IDENTITY);
            log.info(formatLogString(request));
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
            this.stop();
        }

        while (!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    log.debug("Abtruply disconnected");
                    manageClientClosure();
                } else {
                    JSONObject request = JsonParser.stringToJSONObject(jsonString);
                    log.info(formatLogString(request));
                    handleClientRequest(request);
                }
            } catch (Exception e) {
                log.debug("Abruptly closed in");
                manageClientClosure();
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
        try {
            DataOutputStream dout = new DataOutputStream(this.socket.getOutputStream());
            dout.write((returnMessage.toJSONString() + "\n").getBytes("UTF-8"));
            dout.flush();
        } catch (IOException e) {
        }
    }

    private void handleQuitRequest(JSONObject message, boolean clientActive) {
        boolean isOwner = this.equals(this.currentChatRoom.getOwner());
        JSONObject clientResponse = null;
        message.put(IDENTITY, this.id);
        message.put(ROOM_ID, this.currentChatRoom.getRoomId());
        while (clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        log.info("[{}] QUIT - Approved: {}", this.id, true);
        if (clientActive) {
            log.debug("Sending quit response to client");
            this.sendResponse(clientResponse);
        }
        try {
            /**
             * is owner
             */
            if (isOwner) {
                this.chatRoomHandler.deleteRoom(this, true, !clientActive);
            } else {
                log.debug("removing client from the room");
                this.currentChatRoom.removeClient(this, "");
            }
        } catch (ClientNotInChatRoomException | ChatroomDoesntExistsException
                | ClientNotOwnerException | ClientAlreadyInChatRoomException e) {
        }
        this.stop();
    }

    private void handleJoinRoom(JSONObject message) {
        message.put(IDENTITY, this.id);
        message.put(FORMER, this.currentChatRoom.getRoomId());
        JSONObject clientResponse = null;
        while (clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(message);
        }
        if (clientResponse.get(TYPE).equals(ROOM_CHANGE)
                && !clientResponse.get(ROOM_ID).equals(clientResponse.get(FORMER))) {
            try {
                this.currentChatRoom.removeClient(this, (String) clientResponse.get(ROOM_ID));
                this.currentChatRoom = chatRoomHandler.getChatroomFromName((String) clientResponse.get(ROOM_ID));
                this.currentChatRoom.addClient(this, (String) clientResponse.get(FORMER));
            } catch (ClientNotInChatRoomException | ClientAlreadyInChatRoomException
                    | ChatroomDoesntExistsException e) {
            }
            log.info("[{}] JOINROOM - Room: {} - Former: {} - Approved: {}", this.id, clientResponse.get(ROOM_ID),
                    clientResponse.get(FORMER), true);
        } else if (clientResponse.get(TYPE).equals(ROUTE)) {
            String roomId = (String) clientResponse.get(ROOM_ID);
            log.info("[{}] ROUTE - ServerId: {} - RoomId: {} - Approved: {}",
                    this.id,
                    SystemState.getChatroomServer(roomId),
                    roomId,
                    true);
            try {
                this.currentChatRoom.removeClient(this, roomId);
                reroute = true;
            } catch (ClientNotInChatRoomException e) {
            }
        } else {
            log.info("[{}] JOINROOM - Room: {} - Approved: {}", this.id, clientResponse.get(ROOM_ID), false);
        }
        this.sendResponse(clientResponse);
    }

    private void manageClientClosure() {
        /**
         * client stops the socket gracefully
         * route
         */
        if (reroute) {
            this.stop();
        }
        /**
         * quit
         */
        else {
            JSONObject jsonObject = ClientMessage.getQuitRequest();
            log.info(formatLogString(jsonObject));
            handleQuitRequest(jsonObject, false);
        }
    }

    private void handleNewIdentity(JSONObject request) {
        String clientId = request.get(IDENTITY).toString();
        JSONObject clientResponse = null;

        while (clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(request);
        }
        log.info("[{}] NEWIDENTITY - Approved: {}", clientId, clientResponse.get(APPROVED));
        this.sendResponse(clientResponse);
        if (Boolean.parseBoolean(clientResponse.get(APPROVED).toString())) {
            try {
                this.setId(clientId);
                this.chatRoomHandler.getMainHall().addClientAndNotify(this, "");
            } catch (ClientAlreadyInChatRoomException e) {
            }
        } else {
            stop();
        }
    }

    private void handleMoveJoin(JSONObject request) {
        this.setId((String) request.get(IDENTITY));
        JSONObject clientResponse = null;
        while (clientResponse == null) {
            clientResponse = this.server.getState().respondToClientRequest(request);
        }

        String roomId = null;
        if (clientResponse.containsKey(ROOM_ID)) {
            roomId = (String) clientResponse.get(ROOM_ID);
            clientResponse.remove(ROOM_ID);
        }
        this.sendResponse(clientResponse);


        if (Boolean.parseBoolean((String) clientResponse.get(APPROVED))) {
            log.info("[{}] MOVEJOIN - RoomId: {} - Approved: {}", request.get(IDENTITY).toString(),
                    roomId, true);
            try {
                String prevRoomId = (String) request.get(FORMER);
                this.currentChatRoom = chatRoomHandler.getChatroomFromName(roomId);
                try {
                    this.currentChatRoom.addClientAndNotify(this, prevRoomId);
                } catch (ClientAlreadyInChatRoomException e) {
                }
            } catch (ChatroomDoesntExistsException e) {
            }
        } else {
            log.info("[{}] MOVEJOIN - Approved: {}", request.get(IDENTITY).toString(), false);
            stop();
        }
    }

    private void handleList() {
        JSONObject response = ServerMessage
                .getRoomListResponse(SystemState.getChatRooms());
        sendResponse(response);
    }

    private void handleWho() {
        ClientThread owner = currentChatRoom.getOwner();
        String ownerName = owner != null ? owner.getId() : "";
        JSONObject response = ServerMessage.getWhoResponse(
                currentChatRoom.getRoomId(),
                currentChatRoom.getClientIds(),
                ownerName);
        sendResponse(response);
    }

    private void handleMessage(JSONObject message) {
        String content = (String) message.get(CONTENT);
        try {
            currentChatRoom.sendMessage(content, this);
        } catch (ClientNotInChatRoomException e) {
        }
    }

    private void handleDeleteRoom(JSONObject message) {
        message.put(IDENTITY, this.getId());
        JSONObject deleteRoomResponse = null;
        JSONObject response = null;


        while (deleteRoomResponse == null) {
            deleteRoomResponse = this.server.getState().respondToClientRequest(message);
        }
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
                }
            } else {
                log.info("[{}] DELETEROOM - RoomId: {} - Approved: {}", this.id, response.get(ROOM_ID),
                        response.get(APPROVED));
            }
            sendResponse(response);
        }
    }

    private void handleCreateroom(JSONObject message) {
        JSONObject createRoomResponse = null;
        JSONObject response = null;
        message.put(IDENTITY, this.getId());

        while (createRoomResponse == null) {
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
                }
            } else {
                log.info("[{}] CREATEROOM - RoomId: {} - Approved: {}", this.id, response.get(ROOM_ID), response.get(APPROVED));
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

    private String formatLogString(JSONObject request) {
        String type = (String) request.get(TYPE);
        switch (type) {
            case NEW_IDENTITY -> {
                return String.format("[%s] NEWIDENTITY", request.get(IDENTITY));
            }
            case MOVE_JOIN -> {
                return String.format("[%s] MOVEJOIN - RoomId: %s - Former: %s",
                        request.get(IDENTITY), request.get(ROOM_ID), request.get(FORMER));
            }
            case LIST -> {
                return String.format("[%s] LIST", this.id);
            }
            case WHO -> {
                return String.format("[%s] WHO", this.id);
            }
            case CREATE_ROOM -> {
                return String.format("[%s] CREATEROOM - RoomId: %s", this.id, request.get(ROOM_ID));
            }
            case JOIN_ROOM -> {
                return String.format("[%s] JOINROOM - RoomId: %s", this.id, request.get(ROOM_ID));
            }
            case DELETE_ROOM -> {
                return String.format("[%s] DELETEROOM - RoomId: %s", this.id, request.get(ROOM_ID));
            }
            case MESSAGE -> {
                String msg = (String) request.get(CONTENT);
                return String.format("[%s] MESSAGE - Msg: %s ", this.id, msg.substring(0, Math.min(15, msg.length())));
            }
            case QUIT -> {
                return String.format("[%s] QUIT", this.id);
            }
            default -> {
                return String.format("[%s] UNSUPPORTED REQUEST TYPE", this.id);
            }
        }
    }
}
