package com.ds.chatserver.utils;

public class Validation {
    public static boolean validateClientID(String identity) {
        // validate the format
        // validate for the uniqueness
        return true;
    }

    public static boolean validateRoomID(String identity) {
        if (identity.length() < 3 || identity.length() > 16) {
            return false;
        }
        return Character.isAlphabetic(identity.charAt(0)) && Util.isAlphaNumeric(identity);
    }
}
