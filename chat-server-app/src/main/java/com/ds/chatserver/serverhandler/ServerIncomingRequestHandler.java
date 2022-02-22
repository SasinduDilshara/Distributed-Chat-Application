package com.ds.chatserver.serverhandler;

import com.ds.chatserver.clienthandler.ClientRequestHandler;
import com.ds.chatserver.utils.JsonParser;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ServerIncomingRequestHandler implements Runnable{
    private final Socket socket;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;
    private Boolean exit;
    private static final Logger logger = LoggerFactory.getLogger(ServerIncomingRequestHandler.class);

    public ServerIncomingRequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.exit = false;
    }

    @Override
    public void run() {
        String jsonString = null;
        logger.info("New Connection Started with {}", socket.getPort());
        DataOutputStream dout = null;
        try {
            dout = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    this.stop();
                    break;
                } else {
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                    String type = (String)jsonObject.get("type");
                    logger.info(jsonObject.toJSONString());
                    JSONObject response = new JSONObject();
                    response.put("type", type);
                    response.put("reply", "Hello There!");
                    dout.write((response.toJSONString() + "\n").getBytes("UTF-8"));
                    dout.flush();
//                    switch (type){
//
//                    }
                    // TODO
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.exit = true;
    }
}
