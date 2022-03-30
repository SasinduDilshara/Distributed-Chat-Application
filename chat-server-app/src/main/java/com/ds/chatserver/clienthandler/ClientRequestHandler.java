package com.ds.chatserver.clienthandler;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;


public class ClientRequestHandler {

    private Integer socketPort;
    private ServerSocket serverSocket;
    private Server server;
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public ClientRequestHandler(Server server) throws IOException {
        this.server = server;
        this.socketPort = ServerConfigurations.getServerDetails(server.getServerId()).getClientPort();
        serverSocket = new ServerSocket(socketPort);
        this.start();
    }

    public void setSocketPort(Integer socketPort) {
        this.socketPort = socketPort;
    }

    public ServerSocket getSocket() {
        return this.serverSocket;
    }

    public void start() {
        while (true) {
            try {
                Thread thread = new Thread(new ClientThread(serverSocket.accept(), this.server));
                thread.start();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        // Destroy all the resources
    }


}
