package com.ds.chatserver.serverstate;

import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

@Slf4j
public class FollowerState extends ServerState {

    public FollowerState(Server server) {
        super(server);
        log.info("Follower State : {}", this.server.getCurrentTerm());
    }

    @Override
    public void heartBeatAndLeaderElect() {

    }

    @Override
    public void changeState(Server server) {}

    @Override
    public JSONObject handleRequestVote(JSONObject jsonObject) {
        boolean voteGranted;
        int requestVoteTerm = Integer.parseInt((String) jsonObject.get("term"));

        if (requestVoteTerm <= this.server.getCurrentTerm()) {
            voteGranted = false;
        }
        /**
         * Qualified for voting
         * */
        else if (this.server.getLastVotedTerm() < this.server.getCurrentTerm()) {
            //TODO: recheck condition
            if ((this.server.getLastLogIndex() <= (Integer.parseInt((String) jsonObject.get("lastLogIndex"))))
                    && (this.server.getLastLogTerm() <= Integer.parseInt((String) jsonObject.get("lastLogTerm")))) {
                voteGranted = true;
                this.server.setLastVotedServerId((String) jsonObject.get("candidateId"));
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
}
