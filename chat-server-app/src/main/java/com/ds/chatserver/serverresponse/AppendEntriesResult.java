package com.ds.chatserver.serverresponse;

public class AppendEntriesResult {
    private int term;
    private Boolean success;

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
}
