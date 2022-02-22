package com.ds.chatserver.serverhandler;

import com.ds.chatserver.config.ServerConfigurations;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ServerRequestSendTask implements Callable<JSONObject> {
    private JSONObject jsonMessage;
    private JSONParser parser;
    private String serverId;
    private static final Logger logger = LoggerFactory.getLogger(ServerIncomingRequestListener.class);

    public ServerRequestSendTask(String serverId, JSONObject jsonMessage) throws IOException {
        this.jsonMessage = jsonMessage;
        this.serverId = serverId;
        this.parser = new JSONParser();
    }

    @Override
    public JSONObject call()  {

        JSONObject response = null;
        DataOutputStream dataOutputStream;
        int numberOfReTrys = 0;
        int timeout = 100;

        int port = ServerConfigurations.getServerDetails(serverId).getServerPort();
        String host = ServerConfigurations.getServerDetails(serverId).getIpAddress();
        Socket socket = null;

        while(socket == null && numberOfReTrys < 10) {
            try {
                socket = new Socket(host, port);

            } catch (IOException e) {
                logger.error(
                        "Initializing Socket Connection Failed, receiverId : {}, Number of retry {}",
                        serverId,
                        numberOfReTrys);
            }
            if (socket == null) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            numberOfReTrys++;
            timeout = timeout*2;
        }
        if (socket == null) {
            return null;
        }

        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((this.jsonMessage.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            response = (JSONObject) this.parser.parse(bufferedReader.readLine());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return response;
    }
}
