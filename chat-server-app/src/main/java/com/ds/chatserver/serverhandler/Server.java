package com.ds.chatserver.serverhandler;

import com.ds.chatserver.chatroom.ChatRoom;
import com.ds.chatserver.chatroom.ChatRoomHandler;
import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.exceptions.ChatroomAlreadyExistsException;
import com.ds.chatserver.exceptions.ClientNotInChatRoomException;
import com.ds.chatserver.exceptions.InvalidChatroomException;
import com.ds.chatserver.log.RaftLog;
import com.ds.chatserver.serverstate.CandidateState;
import com.ds.chatserver.serverstate.FollowerState;
import com.ds.chatserver.serverstate.ServerState;
import com.ds.chatserver.systemstate.ChatroomLog;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.Util;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

@Setter
@Getter
@Slf4j
public class Server {
    private ServerState state;
    private String serverId;
    private int currentTerm = 0;
    private int lastVotedTerm = -1;
    private String leaderId = null;
    private String lastVotedServerId = null;
    private RaftLog raftLog;

    public Server(String serverId) {
        this.serverId = serverId;
        this.raftLog = new RaftLog();

        createMainhall();
    }
    private void createMainhall(){
        Set<String> serverIds = ServerConfigurations.getServerIds();

        for (String id: serverIds) {
            if(id.equals(this.getServerId())){
                try {
//                   // TODO Consider owerner null case
                    ChatRoomHandler.getInstance().createChatRoom(Util.getMainhall(id),null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            SystemState.addChatroom(new ChatroomLog(Util.getMainhall(id), id, ""));
        }
    }

    public void incrementTerm() {
        this.currentTerm ++;
    }

    public Boolean init(String serverId) {
        this.state = new FollowerState(this, null);

        Integer serverPort = ServerConfigurations.getServerDetails(serverId).getServerPort();
        try {
            Thread requestListener = new Thread(new ServerIncomingRequestListener(serverPort, this));
            requestListener.start();
        } catch (IOException e) {
            log.error("");
            e.printStackTrace();
        }

        return true;
    }

    public JSONObject handleServerRequest(JSONObject jsonObject) {
        return this.getState().respondToServerRequest(jsonObject);
    }

    public void setState(ServerState state){
        this.state.stop();
        this.state = state;
    }

}
