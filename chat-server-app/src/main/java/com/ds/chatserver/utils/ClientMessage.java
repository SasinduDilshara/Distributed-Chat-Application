package com.ds.chatserver.utils;

import org.json.simple.JSONObject;

import static com.ds.chatserver.constants.ClientRequestTypeConstants.QUIT;
import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.TYPE;

public class ClientMessage {
    @SuppressWarnings("unchecked")
    public static JSONObject getQuitRequest() {
        JSONObject quit = new JSONObject();
        quit.put(TYPE, QUIT);
        return quit;
    }
}
