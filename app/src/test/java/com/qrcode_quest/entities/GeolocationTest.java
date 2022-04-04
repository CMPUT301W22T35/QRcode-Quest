package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeolocationTest {

    @Test
    public void testSetGets() {
        Geolocation A = new Geolocation(53.525759764114554, -113.52710655047908);  // UofA
        A.setLatitude(0);
        A.setLongitude(50);
        assertEquals(0, A.getLatitude(), 0);
        assertEquals(50, A.getLongitude(), 0);
    }

    @Test
    public void testGetDistance() {
        // data obtained from google map
        Geolocation A = new Geolocation(53.525759764114554, -113.52710655047908);  // UofA
        Geolocation B = new Geolocation(53.57199227917908, -113.5048717626592);  // NAIT
        double distance = A.getDistanceFrom(B);
        assertEquals(5350.0, A.getDistanceFrom(B), 20.0);

        // exchange destinations should not affect distance
        assertEquals(A.getDistanceFrom(B), B.getDistanceFrom(A), 0.01);

        // distance to itself is 0
        assertEquals(0.0, A.getDistanceFrom(A), 0.0);

        // two ends of earth
        Geolocation C = new Geolocation(-11.670582779935541, 27.475628679561044); // Lubumbashi
        assertEquals(14228540, C.getDistanceFrom(A), 700000);

        // degenerate case: latitude = 0 or latitude = 90
        Geolocation D = new Geolocation(0, 180);
        Geolocation E = new Geolocation(0, 0);
        Geolocation F = new Geolocation(90, 180);
        Geolocation G = new Geolocation(90, 0);
        assertEquals(Geolocation.getEarthRadius() * Math.PI, D.getDistanceFrom(E), 700000);
        assertEquals(0, F.getDistanceFrom(G), 0.1);

        // degenerate case: longitude is equal for both points;
        Geolocation H = new Geolocation(-90, 10);
        Geolocation I = new Geolocation(90, 10);
        assertEquals(Geolocation.getEarthRadius() * Math.PI, H.getDistanceFrom(I), 700000);

        // compute input that warps around 360 degrees
        for (int i = 1; i <= 10; i++) {
            A.setLongitude(A.getLongitude() + 360);
            A.setLatitude(A.getLatitude() - 720);
            B.setLongitude(B.getLongitude() - 360);
            B.setLatitude(B.getLatitude() - 360);
            assertEquals(5350.0, A.getDistanceFrom(B), 20.0);
        }
    }
}
