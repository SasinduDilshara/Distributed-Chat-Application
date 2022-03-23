package com.ds.chatserver.utils;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

public class Util {
    public static String getMainhall(String serverId) {
        return "MainHall-" + serverId;
    }

    public static ArrayList<Event> decodeJsonEventList(JSONArray jsonEntries) {
        ArrayList<Event> eventObjectList = new ArrayList<>();

        for (Object jsonEntryObject : jsonEntries) {
            JSONObject jsonEntry = (JSONObject) jsonEntryObject;

            String clientId = (String) jsonEntry.get(CLIENT_ID);
            String serverId = (String) jsonEntry.get(SERVER_ID);
            EventType type = EventType.valueOf((String) jsonEntry.get(TYPE));
            int logIndex = Integer.parseInt((String) jsonEntry.get(LOG_INDEX));
            int logTerm = Integer.parseInt((String) jsonEntry.get(TERM));
            String parameter = (String) jsonEntry.get(PARAMETER);

            Event event = Event.builder()
                    .clientId(clientId)
                    .serverId(serverId)
                    .type(type)
                    .logIndex(logIndex)
                    .logTerm(logTerm)
                    .parameter(parameter)
                    .build();

            eventObjectList.add(event);
        }

        return eventObjectList;
    }

    public static boolean isAlphaNumeric(String name) {
        return name != null && name.matches("^[a-zA-Z0-9]*$");
    }
}
