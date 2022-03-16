package com.ds.chatserver.serverstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.LogEntryStatus;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerMessage;
import com.ds.chatserver.utils.ServerServerMessage;
import com.ds.chatserver.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.ServerConfigurationConstants.ELECTION_TIMEOUT;

@Slf4j
public class FollowerState extends ServerState {
    private Timestamp lastHeartBeatTimestamp;

    public FollowerState(Server server, String leaderId) {
        super(server);
        this.server.setLeaderId(leaderId);
        lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());
        this.initState();
        log.info("Follower State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
    }


    @Override
    public void heartBeatAndLeaderElect() {
        Timestamp expireTimestamp = new Timestamp(System.currentTimeMillis() - ELECTION_TIMEOUT);
        if(expireTimestamp.after(lastHeartBeatTimestamp)){
            log.info("HB Timeout Last:{} Current:{} Expire: {}", lastHeartBeatTimestamp, new Timestamp(System.currentTimeMillis()), expireTimestamp);
            this.server.setState(new CandidateState(this.server));
        }
    }

    @Override
    public JSONObject handleRequestVote(JSONObject jsonObject) {
//      TODO: Votelock instead of synchronized ?
        this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());
        boolean voteGranted;
        int requestVoteTerm = Integer.parseInt((String) jsonObject.get(TERM));

        if (requestVoteTerm < this.server.getCurrentTerm()) {
            voteGranted = false;
        }
        /**
         * Qualified for voting
         * */
        else if (this.server.getLastVotedTerm() < this.server.getCurrentTerm()) {
            //TODO: recheck condition
            if ((this.server.getRaftLog().getLastLogIndex() <= (Integer.parseInt((String) jsonObject.get(LAST_LOG_INDEX))))
                    && (this.server.getRaftLog().getLastLogTerm() <= Integer.parseInt((String) jsonObject.get(LAST_LOG_TERM)))) {
                voteGranted = true;
                this.server.setLastVotedServerId((String) jsonObject.get(CANDIDATE_ID));
                this.server.setLastVotedTerm(requestVoteTerm);
            } else {
                voteGranted = false;
            }
        } else {
            voteGranted = false;
        }
        if (requestVoteTerm > this.server.getCurrentTerm()) {
            this.server.setCurrentTerm(requestVoteTerm);
        }
        JSONObject response = ServerServerMessage.getRequestVoteResponse(this.server.getCurrentTerm(), voteGranted);
        return response;
    }

    @Override
    public synchronized void initState() {
        notifyAll();
    }

    @Override
    public synchronized JSONObject handleRequestAppendEntries(JSONObject jsonObject) {
//        moved to the else clause
//        this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        int prevLogIndex = Integer.parseInt((String)jsonObject.get(PREVIOUS_LOG_INDEX));
        int prevLogTerm = Integer.parseInt((String)jsonObject.get(PREVIOUS_LOG_TERM));
        int leaderCommit = Integer.parseInt((String)jsonObject.get(LEADER_COMMIT));
        String leaderId = (String) jsonObject.get(LEADER_ID);

        ArrayList<Event> logEntries = Util.decodeJsonEventList((JSONArray) jsonObject.get(ENTRIES));
        Boolean success = false;
        int[] resultLogStatus;

        if (requestTerm < server.getCurrentTerm()) {
            /*
            Reply false if term < currentTerm
             */
            success = false;
        } else {
            this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

            resultLogStatus = server.getRaftLog().checkLogIndexWithTerm(prevLogIndex, prevLogTerm);
            if (resultLogStatus[0] == LogEntryStatus.NOT_FOUND) {
                /*
                Reply false if log doesn’t contain an entry at prevLogIndex
                    whose term matches prevLogTerm
                 */
                success = false;
            } else {
                if (resultLogStatus[0] == LogEntryStatus.CONFLICT) {
                    /*
                    If an existing entry conflicts with a new one (same index
                    but different terms), delete the existing entry and all that
                    follow it
                     */
                    server.getRaftLog().deleteEntriesFromIndex(resultLogStatus[1]);
                }
                /*
                Append any new entries not already in the log
                 */
                server.getRaftLog().appendLogEntries(logEntries);
                /*
                If leaderCommit > commitIndex, set commitIndex =
                    min(leaderCommit, index of last new entry)
                 */
                if (leaderCommit > server.getRaftLog().getCommitIndex()) {
                    server.getRaftLog().setCommitIndex(Math.min(leaderCommit,
                            server.getRaftLog().getLastLogIndex()));
                    SystemState.commit(this.server);
                }
                success = true;
            }
        }
//        TODO chech codition
        if (requestTerm >= this.server.getCurrentTerm()) {
            this.server.setCurrentTerm(requestTerm);
            this.server.setLeaderId(leaderId);
        }

        JSONObject response = ServerServerMessage.getAppendEntriesResponse(
                this.server.getCurrentTerm(),
                success
        );

        return response;
    }

    @Override
    public String printState(){
        return "Follower State - Term: " + this.server.getCurrentTerm()
                + " Leader: " + this.server.getLeaderId()
                + " LastLogIndex: " + this.server.getRaftLog().getLastLogIndex();
    }

    @Override
    public JSONObject respondToNewIdentity(JSONObject request){
        log.info(request.toString());
        JSONObject requestToLeader = ServerServerMessage.getCreateClientRequest(
                this.server.getCurrentTerm(),
                (String) request.get(IDENTITY),
                this.server.getServerId()
        );

        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(1);
        Thread thread = null;
        try {
            thread = new Thread(new ServerRequestSender( this.server.getLeaderId(), requestToLeader, queue));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread.start();

        try {
            JSONObject response = queue.take();
            log.debug("Response from leader: {}", response.toString());

            if ((Boolean) response.get(ERROR)) {
                return ServerMessage.getNewIdentityResponse(false);
            } else {
                return ServerMessage.getNewIdentityResponse((Boolean) response.get(SUCCESS));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected JSONObject respondToDeleteRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID_2);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject requestToLeader = ServerServerMessage.getDeleteRoomRequest(
                this.server.getCurrentTerm(),
                clientId,
                this.server.getServerId(),
                roomId
        );
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(1);
        Thread thread = null;
        try {
            thread = new Thread(new ServerRequestSender(this.server.getLeaderId(), requestToLeader, queue));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread.start();

        try {
            JSONObject response = queue.take();

            if ((Boolean) response.get(SUCCESS)) {
                jsonObjects.add(ServerMessage.getDeleteRoomResponse(
                        roomId,
                        (Boolean) response.get(SUCCESS)
                ));
                jsonObjects.add((ServerMessage.getRoomChangeResponse(
                        clientId,
                        roomId,
                        Util.getMainhall(this.server.getServerId())
                        )));
                return ServerMessage.getJsonResponses(jsonObjects);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsonObjects.add(ServerMessage.getDeleteRoomResponse(
                roomId,
                false
        ));
        return ServerMessage.getJsonResponses(jsonObjects);
    }

    @Override
    protected JSONObject respondToJoinRoom(JSONObject request) {
        return null;
    }

    @Override
    protected JSONObject respondToCreateRoom(JSONObject request) {
        String clientId = (String) request.get(IDENTITY);
        String roomId = (String) request.get(ROOM_ID_2);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject requestToLeader = ServerServerMessage.getCreateChatroomRequest(
                this.server.getCurrentTerm(),
                clientId,
                roomId,
                this.server.getServerId()
        );
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(1);
        Thread thread = null;
        try {
            thread = new Thread(new ServerRequestSender(this.server.getLeaderId(), requestToLeader, queue));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread.start();

        try {
            JSONObject response = queue.take();

            if ((Boolean) response.get(SUCCESS)) {
                jsonObjects.add(ServerMessage.getCreateRoomResponse(
                        roomId,
                        (Boolean) response.get(SUCCESS)
                ));
                jsonObjects.add(ServerMessage.getRoomChangeResponse(
                        clientId,
                        Util.getMainhall(this.server.getServerId()),
                        roomId));
                return ServerMessage.getJsonResponses(jsonObjects);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsonObjects.add(ServerMessage.getCreateRoomResponse(
                roomId,
                false
        ));
        return ServerMessage.getJsonResponses(jsonObjects);
    }

    @Override
    protected JSONObject respondToMoveJoin(JSONObject request) {
        return null;
    }

    @Override
    protected JSONObject respondToQuit(JSONObject request) {
        return null;
    }
}
