package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.sql.Timestamp;

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
        JSONObject response = ServerServerMessage.responseVote(this.server.getCurrentTerm(), voteGranted);
        return response;
    }

    @Override
    public void initState() {

    }

    @Override
    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {
        this.lastHeartBeatTimestamp = new Timestamp(System.currentTimeMillis());

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        String leaderId = (String) jsonObject.get(LEADER_ID);
//        log.info("Append Entry {} from {}", requestTerm, leaderId);
        boolean success = false;
        if (requestTerm >= this.server.getCurrentTerm()) {
//            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setLeaderId(leaderId);
            success = true;
        }
        JSONObject response = ServerServerMessage.responseAppendEntries(
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
