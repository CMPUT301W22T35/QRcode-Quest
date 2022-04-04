package com.qrcode_quest.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 */
public class MapListContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<MapListItem> ITEMS = new ArrayList<MapListItem>();

    /**
     * add item to the static list of nearby codes
     * @param item an item
     */
    public static void addItem(MapListItem item) {
        ITEMS.add(item);
    }

    /** clear all items in the static list */
    public static void clearItems(){
        ITEMS.clear();
    }

    /** sort nearby qr code locations by distance in ascending order */
    public static void sort(){
        // Sort QR code locations by distance (ascending)
        Collections.sort(ITEMS, (m1, m2) -> Double.compare(m1.distance, m2.distance));
    }

    /**
     * A map list item for displaying a QR Code score, distance, and geolocation
     */
    public static class MapListItem {
        public final int score;
        public final double distance;
        public final double latitude;
        public final double longitude;

        public MapListItem(int score, double distance, double lat, double lon) {
            this.score = score;
            this.distance = distance;
            this.latitude = lat;
            this.longitude = lon;
        }
    }
}