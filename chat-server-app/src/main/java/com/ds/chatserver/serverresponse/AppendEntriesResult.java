package com.ds.chatserver.serverresponse;

public class AppendEntriesResult {
    private int term;
    private Boolean success;

    public AppendEntriesResult(int term, Boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public static AppendEntriesResult generateResponse(int term, Boolean success) {
        return new AppendEntriesResult(term, success);
    }
}
