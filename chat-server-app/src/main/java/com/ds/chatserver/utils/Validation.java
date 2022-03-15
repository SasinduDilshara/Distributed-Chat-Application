package com.ds.chatserver.utils;

import com.ds.chatserver.systemstate.SystemState;

public class Validation {
    public static boolean validateClientID(String identity) {
        if (identity.length() < 3 || identity.length() > 16) {
            return false;
        }
        return Character.isAlphabetic(identity.charAt(0)) && Util.isAlphaNumeric(identity);
    }

    public static boolean validateRoomID(String chatRoomName) {
        if (chatRoomName.length() < 3 || chatRoomName.length() > 16) {
            return false;
        }
        return Character.isAlphabetic(chatRoomName.charAt(0)) && Util.isAlphaNumeric(chatRoomName);
    }

    public static boolean isChatroomInSystem(String chatroomName) {
        return SystemState.isChatroomExist(chatroomName);
    }
}
