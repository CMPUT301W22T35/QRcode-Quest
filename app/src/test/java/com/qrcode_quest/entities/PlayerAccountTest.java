package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PlayerAccountTest {
    private PlayerAccount player;

    @Test
    public void testBasicConstructor(){
        PlayerAccount player = new PlayerAccount("Guy");

        assertEquals(player.getUsername(), "Guy");
        assertEquals(player.getEmail(), "");
        assertEquals(player.getPhoneNumber(), "");
        assertFalse(player.isDeleted());
        assertFalse(player.isOwner());
    }

    @Test
    public void testFullConstructor(){
        String username = "Caesar";
        String email = "dog@whisperer.com";
        String phone = "123-456-7890";
        player = new PlayerAccount(username, email, phone, true, true);

        assertEquals(player.getUsername(), "Caesar");
        assertEquals(player.getEmail(), "dog@whisperer.com");
        assertEquals(player.getPhoneNumber(), "123-456-7890");
        assertTrue(player.isDeleted());
        assertTrue(player.isOwner());
    }

    @Before
    public void initPlayer(){
        String username = "Caesar";
        String email = "dog@whisperer.com";
        String phone = "123-456-7890";
        player = new PlayerAccount(username, email, phone, false, false);
    }

    @Test
    public void testConstructor(){
        assertEquals(player.getUsername(), "Caesar");
        assertEquals(player.getEmail(), "dog@whisperer.com");
        assertEquals(player.getPhoneNumber(), "123-456-7890");
        assertFalse(player.isDeleted());
        assertFalse(player.isOwner());
    }

    @Test
    public void testSetGets() {
        player.setEmail("");
        assertEquals(player.getEmail(), "");

        player.setPhoneNumber("");
        assertEquals(player.getPhoneNumber(), "");

        player.setDeleted(true);
        assertTrue(player.isDeleted());

        player.setOwner(true);
        assertTrue(player.isOwner());
    }
}
