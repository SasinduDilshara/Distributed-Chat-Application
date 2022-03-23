package com.ds.chatserver.serverhandler;

import com.ds.chatserver.utils.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerIncomingRequestHandler implements Runnable {
    private final Socket socket;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;
    private Server server;

    public ServerIncomingRequestHandler(Socket socket, Server server) throws IOException {
        this.server = server;
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        String jsonString = null;
        DataOutputStream dout = null;
        try {
            dout = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            this.stop();
        }

        try {
            if ((jsonString = bufferedReader.readLine()) == null) {
                this.stop();
            } else {
                JSONObject jsonObject = JsonParser.stringToJSONObject(jsonString);
                JSONObject response = this.server.handleServerRequest(jsonObject);
                dout.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                dout.flush();
            }
        } catch (IOException e) {
        }
        this.stop();
    }

    public void stop() {
        try {
            this.socket.close();
        } catch (IOException e) {
        }
    }
}
