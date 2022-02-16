package com.ds.chatserver.serverhandler;

import com.ds.chatserver.clienthandler.ClientThread;

import java.net.Socket;
import java.util.HashMap;

public class ServerDetails {
    private String serverName;
    private int port;
    private String ipAddress;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
