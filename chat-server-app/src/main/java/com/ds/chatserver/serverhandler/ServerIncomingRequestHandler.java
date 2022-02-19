package com.ds.chatserver.serverhandler;

import com.ds.chatserver.utils.JsonParser;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerIncomingRequestHandler implements Runnable{
    private final Socket socket;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;
    private Boolean exit;

    public ServerIncomingRequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.exit = false;
    }

    @Override
    public void run() {
        String jsonString = null;

        while(!exit) {
            try {
                if ((jsonString = bufferedReader.readLine()) == null) {
                    this.stop();
                    break;
                } else {
                    JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                    String type = (String)jsonObject.get("type");

                    switch (type){

                    }
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
