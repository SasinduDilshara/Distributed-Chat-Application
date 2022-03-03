package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

//    public List<JSONObject> broadCastMessage(String myServerId,JSONObject message) throws ExecutionException, InterruptedException {
//        List<JSONObject> responses = new ArrayList<>();
//        List<Future> responseFutures = new ArrayList<>();
//        Set<String> serverIds = ServerConfigurations.getServerIds();
//
//        for (String id: serverIds) {
//            if (id.equals(myServerId)) {
//                continue;
//            }
//            try {
//                responseFutures.add(executorService.submit(new ServerRequestSender(id, message)));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        for (Future future: responseFutures) {
//            responses.add((JSONObject) future.get());
//        }
//
//
//        return responses;
//    }
//
//    public static void broadCastMessage(ArrayBlockingQueue<JSONObject> queue, JSONObject message) {
//
//    }

}
