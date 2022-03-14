package com.qrcode_quest.entities;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

/**
 * Represents a geolocation (longitude, latitude) on earth
 * @author tianming
 * @version 1.0
 */
public class Geolocation {
    private double latitude;
    private double longitude;
    static private final double EARTH_RADIUS = 6371000;  // in meters

    /**
     * return the earth radius constant used in computing distance between two points
     * @return radius of the earth in meters
     */
    static public double getEarthRadius() { return EARTH_RADIUS; }

    /**
     * creates a Geolocation object representing the given location
     * @param longitude longitude of the location in degrees
     * @param latitude latitude of the location in degrees
     */
    public Geolocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * get the longitude of the location
     * @return longitude in degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * set the longitude of the location
     * @param longitude longitude in degrees
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * get the latitude of the location
     * @return latitude in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * set the latitude of the location
     * @param latitude latitude in degrees
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * get the geographical distance between two geo-locations
     * @param other the other geolocation
     * @return the distance between two locations in meters
     */
    public double getDistanceFrom(Geolocation other) {
        // formula reference:
        // https://en.wikipedia.org/wiki/Geographical_distance https://en.wikipedia.org/wiki/Great-circle_distance
        // See computational formulas under Great-Circle-Distance

        // first get lonA, lonB, latA, latB in degrees
        double lonA = Math.toRadians(this.longitude);
        double latA = Math.toRadians(this.latitude);
        double lonB = Math.toRadians(other.longitude);
        double latB = Math.toRadians(other.latitude);

        double diffLon = lonB - lonA;
        double temp = Math.sin(latA) * Math.sin(latB) + Math.cos(latA) * Math.cos(latB) * Math.cos(diffLon);
        // clamp temp within [-1, 1] to prevent rounding error leading to invalid acos argument
        temp = Math.max(-1.0, Math.min(1.0, temp));
        // get central angle
        double diffRad = Math.acos(temp);
        return EARTH_RADIUS * Math.abs(diffRad);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString(){
        return String.format("%f, %f", this.latitude, this.longitude);
    }
}
