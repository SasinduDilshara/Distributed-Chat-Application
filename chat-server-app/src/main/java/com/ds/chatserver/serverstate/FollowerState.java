package com.ds.chatserver.serverstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.LogEntryStatus;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.systemstate.SystemState;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;
import static com.ds.chatserver.constants.ServerConfigurationConstants.ELECTION_TIMEOUT;

@Slf4j
public class FollowerState extends ServerState {
    private Timestamp lastHeartBeatTimestamp;

    public FollowerState(Server server, String leaderId) {
        super(server);
        this.server.setLeaderId(leaderId);
        lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());
        log.info("Follower State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
    }

    @Override
    public void heartBeatAndLeaderElect() {
        Timestamp expireTimestamp = new Timestamp(System.currentTimeMillis() - ELECTION_TIMEOUT);
        if(expireTimestamp.after(lastHeartBeatTimestamp)){
            log.info("HB Timeout Last:{} Current:{} Expire: {}", lastHeartBeatTimestamp, new Timestamp(System.currentTimeMillis()), expireTimestamp);
            this.server.setState(new CandidateState(this.server));
        }
//        while(true){
//            log.info("Follower State: Term:{} leader:{}", this.server.getCurrentTerm(), this.server.getLeaderId());
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public JSONObject handleRequestVote(JSONObject jsonObject) {
        this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());
        boolean voteGranted;
        int requestVoteTerm = Integer.parseInt((String) jsonObject.get(TERM));

        if (requestVoteTerm <= this.server.getCurrentTerm()) {
            voteGranted = false;
        }
        /**
         * Qualified for voting
         * */
        else if (this.server.getLastVotedTerm() < this.server.getCurrentTerm()) {
            //TODO: recheck condition
            if ((this.server.getLastLogIndex() <= (Integer.parseInt((String) jsonObject.get(LAST_LOG_INDEX))))
                    && (this.server.getLastLogTerm() <= Integer.parseInt((String) jsonObject.get(LAST_LOG_TERM)))) {
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
    public void initState() {

    }

    @Override
    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {
        this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        int prevLogIndex = Integer.parseInt((String)jsonObject.get(PREVIOUS_LOG_INDEX));
        int prevLogTerm = Integer.parseInt((String)jsonObject.get(PREVIOUS_LOG_TERM));
        int leaderCommit = Integer.parseInt((String)jsonObject.get(LEADER_COMMIT));
        String leaderId = (String) jsonObject.get(LEADER_ID);

        ArrayList<Event> logEntries = (ArrayList<Event>) jsonObject.get(ENTRIES);
        Boolean success = false;
        int[] resultLogStatus;


        if (requestTerm < server.getCurrentTerm()) {
            /*
            Reply false if term < currentTerm
             */
            success = false;
        } else {
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
                            server.getRaftLog().getIndexFromLastEntry()));
                    SystemState.commit(this.server);
                }
                success = true;
            }
        }
//        TODO chech codition
        if (requestTerm >= this.server.getCurrentTerm()) {
//            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setLeaderId(leaderId);
            success = true;
        }


        JSONObject response = ServerServerMessage.getAppendEntriesResponse(
                this.server.getCurrentTerm(),
                success
        );
        return response;

    }

    @Override
    public String printState(){
        return "Follower State - Term: " + this.server.getCurrentTerm() + " Leader: " + this.server.getLeaderId();
    }
}
