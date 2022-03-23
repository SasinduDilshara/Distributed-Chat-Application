package com.ds.chatserver.config;

import com.ds.chatserver.serverhandler.ServerDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import static com.ds.chatserver.constants.ServerConfigurationConstants.*;

public class ServerConfigurations {
    private static HashMap<String, ServerDetails> serverDetails;

    public static ServerDetails getServerDetails(String serverId) {
        return serverDetails.get(serverId);
    }

    public static Set<String> getServerIds() {
        return serverDetails.keySet();
    }

    public static int getNumberOfServers() {
        return serverDetails.size();
    }

    public static void loadServerDetails(String filePath) throws
            FileNotFoundException,
            ArrayIndexOutOfBoundsException,
            NumberFormatException {

        File file = new File(filePath);
        Scanner sc = new Scanner(file);
        serverDetails = new HashMap<String, ServerDetails>();

        while (sc.hasNextLine()) {
            String[] tokens = sc.nextLine().split("\t");

            ServerDetails sd = ServerDetails.builder()
                    .serverId(tokens[SERVER_ID])
                    .ipAddress(tokens[IP_ADDRESS])
                    .serverPort(Integer.parseInt(tokens[SERVER_PORT]))
                    .clientPort(Integer.parseInt(tokens[CLIENT_PORT]))
                    .build();

            serverDetails.put(tokens[SERVER_ID], sd);
        }
    }
}
