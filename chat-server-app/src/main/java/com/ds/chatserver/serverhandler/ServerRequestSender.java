package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.ERROR;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.RECEIVER_ID;


public class ServerRequestSender extends Thread {
    private JSONObject jsonMessage;
    private JSONParser parser;
    private String serverId;
    private ArrayBlockingQueue<JSONObject> responseQueue;
    private int maxRetries;

    public ServerRequestSender(String serverId, JSONObject jsonMessage, ArrayBlockingQueue<JSONObject> responseQueue) throws IOException {
        this.jsonMessage = jsonMessage;
        this.serverId = serverId;
        this.responseQueue = responseQueue;
        this.parser = new JSONParser();
        this.maxRetries = 3;
    }

    public ServerRequestSender(String serverId, JSONObject jsonMessage, ArrayBlockingQueue<JSONObject> responseQueue, int maxRetries) throws IOException {
        this.jsonMessage = jsonMessage;
        this.serverId = serverId;
        this.responseQueue = responseQueue;
        this.parser = new JSONParser();
        this.maxRetries = maxRetries;
    }

    @Override
    public void run() {

        JSONObject response = null;
        DataOutputStream dataOutputStream;
        int numberOfReTrys = 0;
        int timeout = 100;

        if (serverId == null) {
            try {
                response = new JSONObject();
                response.put(ERROR, true);
                response.put(RECEIVER_ID, serverId);
                responseQueue.put(response);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int port = ServerConfigurations.getServerDetails(serverId).getServerPort();
        String host = ServerConfigurations.getServerDetails(serverId).getIpAddress();
        Socket socket = null;

        while (socket == null && numberOfReTrys < this.maxRetries) {
            try {
                socket = new Socket(host, port);
            } catch (IOException e) {}

            numberOfReTrys++;
            if (socket == null && numberOfReTrys < this.maxRetries) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                }
            }

            timeout = timeout * 2;
        }
        if (socket == null) {
            try {
                response = new JSONObject();
                response.put(ERROR, true);
                response.put(RECEIVER_ID, serverId);
                responseQueue.put(response);
            } catch (InterruptedException e) {
            }
        } else {
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write((this.jsonMessage.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                response = (JSONObject) this.parser.parse(bufferedReader.readLine());
            } catch (IOException | ParseException e) {
            } finally {
                if (response == null) {
                    response = new JSONObject();
                }
                response.put(RECEIVER_ID, serverId);
                response.put(ERROR, false);
            }
            try {
                responseQueue.put(response);
            } catch (InterruptedException e) {
            }
        }
    }
}
