package com.qrcode_quest.entities;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class QRStringConverterTest {
    String loginPrefix;
    String profilePrefix;

    @Before
    public void setup() {
        loginPrefix = QRStringConverter.getLoginQrPrefix();
        profilePrefix = QRStringConverter.getProfileQrPrefix();
    }

    @Test
    public void testConvertLoginQRString() {
        String playerName1 = "testPlayer1";
        String playerName2 = "";  // empty player name

        String login1 = QRStringConverter.getLoginQRString(playerName1);
        String login2 = QRStringConverter.getLoginQRString(playerName2);

        assertEquals(loginPrefix + "testPlayer1", login1);
        assertEquals(loginPrefix, login2);
        assertEquals("testPlayer1", QRStringConverter.getPlayerNameFromLoginQRString(login1));
        assertEquals("", QRStringConverter.getPlayerNameFromLoginQRString(login2));

        // null return on incorrectly formatted string
        assertNull(QRStringConverter.getPlayerNameFromLoginQRString("not a qr prefix"));
        assertNull(QRStringConverter.getPlayerNameFromLoginQRString(profilePrefix + "test"));  // possible collision
        assertNull(QRStringConverter.getPlayerNameFromLoginQRString(""));
    }

    @Test
    public void testConvertProfileQRString() {
        String playerName1 = "testPlayer1";
        String playerName2 = "";  // empty player name

        String profile1 = QRStringConverter.getProfileQRString(playerName1);
        String profile2 = QRStringConverter.getProfileQRString(playerName2);

        assertEquals(profilePrefix + "testPlayer1", profile1);
        assertEquals(profilePrefix, profile2);
        assertEquals("testPlayer1", QRStringConverter.getPlayerNameFromProfileQRString(profile1));
        assertEquals("", QRStringConverter.getPlayerNameFromProfileQRString(profile2));

        // null return on incorrectly formatted string
        assertNull(QRStringConverter.getPlayerNameFromProfileQRString("not a qr prefix"));
        assertNull(QRStringConverter.getPlayerNameFromProfileQRString(loginPrefix + "test"));
        assertNull(QRStringConverter.getPlayerNameFromProfileQRString(""));
    }
}
