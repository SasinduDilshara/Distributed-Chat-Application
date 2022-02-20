package com.ds.chatserver.serverresponse;

public class RequestVoteResult {
    private int term;
    private Boolean voteGranted;

    public RequestVoteResult(int term, Boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Boolean getVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(Boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public static RequestVoteResult generateResponse(int term, Boolean success) {
        return new RequestVoteResult(term, success);
    }
}
