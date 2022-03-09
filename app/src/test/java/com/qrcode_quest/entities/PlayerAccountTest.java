package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlayerAccountTest {
    @Test
    public void testBasicConstructor(){
        PlayerAccount player = new PlayerAccount("Guy");

        assertEquals(player.getUsername(), "Guy");
        assertEquals(player.getEmail(), "");
        assertEquals(player.getPhoneNumber(), "");
    }

    @Test
    public void testSetGets() {
        String username = "Caesar";
        String email = "dog@whisperer.com";
        String phone = "123-456-7890";
        PlayerAccount player = new PlayerAccount(username, email, phone);

        assertEquals(player.getUsername(), username);
        assertEquals(player.getEmail(), email);
        assertEquals(player.getPhoneNumber(), phone);

        player.setEmail("");
        assertEquals(player.getEmail(), "");

        player.setPhoneNumber("");
        assertEquals(player.getPhoneNumber(), "");
    }
}
