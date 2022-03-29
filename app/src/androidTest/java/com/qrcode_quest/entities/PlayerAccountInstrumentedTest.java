package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PlayerAccountInstrumentedTest {

    private PlayerAccount player;

    @Before
    public void initPlayer(){
        String username = "Caesar";
        String email = "dog@whisperer.com";
        String phone = "123-456-7890";
        player = new PlayerAccount(username, email, phone, false, false);
    }

    @Test
    public void testParceling(){
        // Write the player to parcel
        Parcel parcel = Parcel.obtain();
        player.writeToParcel(parcel, player.describeContents());

        // Reset the cursor
        parcel.setDataPosition(0);

        // Build a new player using the parcel
        PlayerAccount builtPlayer = PlayerAccount.CREATOR.createFromParcel(parcel);

        // Compare original and built player
        assertEquals(builtPlayer.getUsername(), player.getUsername());
        assertEquals(builtPlayer.getEmail(), player.getEmail());
        assertEquals(builtPlayer.getPhoneNumber(), player.getPhoneNumber());
        assertEquals(builtPlayer.isOwner(), player.isOwner());
        assertEquals(builtPlayer.isDeleted(), player.isDeleted());
    }
}
