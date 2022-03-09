package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class QRShotTest {
    @Test
    public void testFullConstructor(){
        QRShot shot = new QRShot("Dad", "randomhash",
                null, new Geolocation(-0.1, 0.1));

        assertNotEquals(shot.getName(), "");
        assertEquals(shot.getOwnerName(), "Dad");
        assertEquals(shot.getCodeHash(), "randomhash");
        assertEquals(shot.getLocation(), new Geolocation(-0.1, 0.1));

        // Note: Photo is not tested as it requires static functions building a bitmap
        assertEquals(shot.getPhoto(), null);
    }

    @Test
    public void testBasicConstructor(){
        QRShot shot = new QRShot("Dad", "randomhash");
        assertNotEquals(shot.getName(), "");
        assertEquals(shot.getOwnerName(), "Dad");
        assertEquals(shot.getCodeHash(), "randomhash");
        assertEquals(shot.getPhoto(), null);
        assertEquals(shot.getLocation(), null);
    }

    @Test
    public void testGetSets(){
        QRShot shot = new QRShot("Dad", "randomhash");

        // Set and test location
        shot.setLocation(new Geolocation(1.0, -1.0));
        assertEquals(shot.getLocation(), new Geolocation(1.0, -1.0));

        // Note: Photo is not tested as it requires static functions building a bitmap
        shot.setPhoto(null);
        assertEquals(shot.getPhoto(), null);
    }
}
