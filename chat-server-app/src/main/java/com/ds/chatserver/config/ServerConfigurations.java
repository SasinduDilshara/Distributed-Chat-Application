package com.ds.chatserver.config;

import com.ds.chatserver.serverhandler.ServerDetails;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerConfigurations {
    private static HashMap<String, ServerDetails> serverDetails;

    public static ServerDetails getServerDetails(String serverId) {
        return null;
    }

    public void setServerDetails(HashMap<String, ServerDetails> serverDetails) {
        this.serverDetails = serverDetails;
    }

    public static void loadServerDetails() {
        //TODO: read file and append each server details as ServerDetails object to this.serverDetails arraylist
        serverDetails = null;
    }
}
