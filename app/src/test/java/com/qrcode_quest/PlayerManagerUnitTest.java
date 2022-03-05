package com.qrcode_quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlayerManagerUnitTest {
    private String currentPlayerName;
    private String contactInfo;

    @Test
    public void TestClosure() {
        currentPlayerName = "playerA";
        contactInfo = null; // we want to know this

        // boolean isOwner = false;  // see below
        final boolean[] isOwner = {false};

        PlayerManager.getPlayer(currentPlayerName, new PlayerManager.PlayerResult() {
            @Override
            public void handlePlayerResult(PlayerAccount player) {
                player.setContactInfo("some contact info");
                player.setOwner(true);

                // we can do things with a player E.g. query player's name
                assertEquals(currentPlayerName, player.getPlayerName());

                // we can access but *NOT* directly change outside local variable
                // isOwner = player.isOwner();  // error: local variables referenced from an inner class must be final or effectively final
                // but we can work around with Android Studio's grammar suggestion
                isOwner[0] = player.isOwner();
                assertTrue(isOwner[0]);

                // or use instance attributes instead like follows
                contactInfo = player.getContactInfo();
                assertEquals("some contact info", contactInfo);
            }
        });
    }
}
