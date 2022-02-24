package com.ds.chatserver.log;

public class Event {
    private EventTypeName type;
    private int term;
    private int logIndex;
    private String clientId;
    private String serverId = null;
    private String chatroomName;

    public Event(int term, int logIndex, EventTypeName type, String clientId) {
        this.term = term;
        this.logIndex = logIndex;
        this.type = type;
        this.clientId = clientId;
    }

    public Event(int term, int logIndex, EventTypeName type, String clientId, String serverId) {
        this.term = term;
        this.logIndex = logIndex;
        this.type = type;
        this.clientId = clientId;
        this.serverId = serverId;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(int logIndex) {
        this.logIndex = logIndex;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public void setChatroomName(String chatroomName) {
        this.chatroomName = chatroomName;
    }

    public EventTypeName getType() {
        return type;
    }

    public void setType(EventTypeName type) {
        this.type = type;
    }
}
