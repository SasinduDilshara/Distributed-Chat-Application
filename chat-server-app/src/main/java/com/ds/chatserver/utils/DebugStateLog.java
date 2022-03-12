package com.ds.chatserver.utils;

import com.ds.chatserver.serverhandler.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugStateLog implements Runnable{

    private Server server;

    public DebugStateLog(Server server){
        this.server = server;
    }

    @Override
    public void run(){
        while(true){
            log.debug(this.server.getState().printState());

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
