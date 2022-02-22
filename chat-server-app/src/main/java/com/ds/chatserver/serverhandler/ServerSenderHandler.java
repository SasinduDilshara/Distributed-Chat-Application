package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerSenderHandler {
    ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(ServerIncomingRequestListener.class);


    public ServerSenderHandler() {
        logger.info("Initialized Server Sender Handler");
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public JSONObject sendRequest(String serverId, JSONObject jsonMessage) {
        logger.info("Sending message to {}",serverId);
        try {
            Future<JSONObject> responseFuture = executorService.submit(new ServerRequestSendTask(serverId, jsonMessage));
            JSONObject response = responseFuture.get();
            logger.info(response.toJSONString());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
