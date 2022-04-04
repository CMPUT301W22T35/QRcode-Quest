package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

/**
 * conversion of player name from and to String message embedded in a displayed qr code for sharing
 *
 * @author tianming
 * @version 1.0
 */
public class QRStringConverter {
    // prefixes for the qr code message; used to recognize the type of message that a qr code contains
    // E.g. login, profile etc.
    static private final String LOGIN_QR_PREFIX = "QRCodeQuest-login://";
    static private final String PROFILE_QR_PREFIX = "QRCodeQuest-profile://";

    /** @return constant string, the login qr code message prefix */
    public static String getLoginQrPrefix() { return LOGIN_QR_PREFIX; }
    /** @return constant string, the profile qr code message prefix */
    public static String getProfileQrPrefix() { return PROFILE_QR_PREFIX; }

    /**
     * get the corresponding login qr code message for a particular player
     * @param playerName the player's name
     * @return the login qr code message
     */
    public static @NonNull String getLoginQRString(@NonNull String playerName) {
        return LOGIN_QR_PREFIX + playerName;
    }

    /**
     * retrieve a player name from a login qr message string created by getLoginQRString()
     * @param loginQRString the login qr message
     * @return the player's name; returns null on malformed string/string with other formats
     */
    public static String getPlayerNameFromLoginQRString(@NonNull String loginQRString) {
        if (loginQRString.startsWith(LOGIN_QR_PREFIX)) {
            return loginQRString.substring(LOGIN_QR_PREFIX.length());
        } else {
            return null;
        }
    }

    /**
     * get the corresponding profile qr code message for a particular player
     * @param playerName the player's name
     * @return the profile qr code message
     */
    public static @NonNull String getProfileQRString(@NonNull String playerName) {
        return PROFILE_QR_PREFIX + playerName;
    }

    /**
     * retrieve a player name from a profile qr message string created by getProfileQRString()
     * @param loginQRString the profile qr message
     * @return the player's name; returns null on malformed string/string with other formats
     */
    public static String getPlayerNameFromProfileQRString(@NonNull String loginQRString) {
        if (loginQRString.startsWith(PROFILE_QR_PREFIX)) {
            return loginQRString.substring(PROFILE_QR_PREFIX.length());
        } else {
            return null;
        }
    }
}
