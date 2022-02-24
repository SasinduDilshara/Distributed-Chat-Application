package com.ds.chatserver.serverhandler;

import com.ds.chatserver.clienthandler.ClientThread;

import java.net.Socket;
import java.util.HashMap;

public class ServerDetails {
    private String serverName;
    private int serverPort;
    private int clientPort;
    private String ipAddress;

    public ServerDetails(String serverName, String ipAddress, int clientPort, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
        this.ipAddress = ipAddress;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getClientPort(){ return clientPort; }

    public void setClientPort(int clientPort){ this.clientPort = clientPort; }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
