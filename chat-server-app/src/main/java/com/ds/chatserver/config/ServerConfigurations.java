package com.ds.chatserver.config;

import com.ds.chatserver.serverhandler.ServerDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ServerConfigurations {
    private static HashMap<String, ServerDetails> serverDetails;

    public static ServerDetails getServerDetails(String serverId) {
        return serverDetails.get(serverId);
    }

    public static int getNumberOfServers(){
        return serverDetails.size();
    }

    public static void loadServerDetails(String filePath) throws
            FileNotFoundException,
            ArrayIndexOutOfBoundsException,
            NumberFormatException {

        File file = new File(filePath);
        Scanner sc = new Scanner(file);
        serverDetails = new HashMap<String, ServerDetails>();

        while (sc.hasNextLine()){
            String[] tokens = sc.nextLine().split("\t");
            ServerDetails sd = new ServerDetails(
                    tokens[0],
                    tokens[1],
                    Integer.parseInt(tokens[2]),
                    Integer.parseInt(tokens[3]));
            serverDetails.put(tokens[0], sd);
        }
    }
}
