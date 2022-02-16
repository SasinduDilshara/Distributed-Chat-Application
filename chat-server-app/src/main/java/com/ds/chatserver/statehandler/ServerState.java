package com.ds.chatserver.statehandler;

import com.ds.chatserver.serverhandler.Server;

public interface ServerState {
    void execute(Server server);
    void changeState(Server server);
}
