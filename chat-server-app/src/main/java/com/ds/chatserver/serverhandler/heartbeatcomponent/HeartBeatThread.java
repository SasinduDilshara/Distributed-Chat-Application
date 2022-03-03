package com.ds.chatserver.serverhandler.heartbeatcomponent;

import com.ds.chatserver.serverhandler.Server;

public class HeartBeatThread implements Runnable{
    private HBTimeOutInvoker hbTimeOutInvoker;
    private Server server;

    public HeartBeatThread(Server server) {
        this.server = server;
        HBTimeOutInvoker HBTimeOutInvoker = new HBTimeOutInvoker();
    }

    @Override
    public void run() {

    }
}
