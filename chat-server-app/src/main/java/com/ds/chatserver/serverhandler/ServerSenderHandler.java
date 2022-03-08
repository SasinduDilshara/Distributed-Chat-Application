package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class ServerSenderHandler {
    ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(ServerIncomingRequestListener.class);


    public ServerSenderHandler() {
        logger.info("Initialized Server Sender Handler");
        this.executorService = Executors.newFixedThreadPool(15);
    }

//    public JSONObject sendRequest(String serverId, JSONObject jsonMessage) {
//        logger.info("Sending message to {}",serverId);
////        try {
////            Future<JSONObject> responseFuture = executorService.submit(new ServerRequestSender(serverId, jsonMessage));
////            JSONObject response = responseFuture.get();
////            logger.info(response.toJSONString());
////            return response;
////        } catch (IOException e) {
////            e.printStackTrace();
////        } catch (ExecutionException e) {
////            e.printStackTrace();
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//
//        return null;
//    }

    public static void broadCastMessage(String myServerId, JSONObject message,
         ArrayBlockingQueue<JSONObject> blockingQueue) throws ExecutionException, InterruptedException, IOException {
        Set<String> serverIds = ServerConfigurations.getServerIds();
        List<ServerRequestSender> senderThreads =  new ArrayList<>();
        ServerRequestSender serverRequestSender;

        for (String id : serverIds) {
            if (id.equals(myServerId)) {
                continue;
            }
            serverRequestSender = new ServerRequestSender(id, message, blockingQueue);
            senderThreads.add(serverRequestSender);
            serverRequestSender.start();
        }
    }

//    public static void broadCastMessage(ArrayBlockingQueue<JSONObject> queue, JSONObject message) {
//
//    }

}
