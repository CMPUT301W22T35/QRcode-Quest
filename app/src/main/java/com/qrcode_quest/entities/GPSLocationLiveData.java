package com.qrcode_quest.entities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

/**
 * responsible for maintaining the user's current GPS location and updates it when requested
 * access to this class should use isPermissionGranted() to check for location permission before
 * observe() method calls
 * this class is not responsible for obtaining location permission
 *
 * @author tianming
 * @version 1.0
 */
public class GPSLocationLiveData extends MutableLiveData<Location> {
    /** used to check permission for GPS service */
    @NonNull private final Context context;
    /** the location manager provides location for the live data below */
    @NonNull private final LocationManager locationManager;
    /** the location listener registered in the LocationManager that updates the livedata */
    private final LocationListener listener;
    /** location won't update unless this minimum time has passed unit: millisecond */
    private long minTimeMs;
    /** location won't update unless the user moves this minimum distance unit: meter */
    private float minDistanceM;

    /**
     * creates a GPSLocationLiveData; on creation the live data will contain a null reference to
     * Geolocation, and location updates will only start after the first observer is attached.
     * @param context the application context that contains permission information
     * @param locationManager manages and provides updates to the geolocation
     */
    public GPSLocationLiveData(@NonNull Context context, @NonNull LocationManager locationManager) {
        super(null);
        this.context = context;
        this.locationManager = locationManager;

        // give some default values
        minTimeMs = 5000;
        minDistanceM = 10;

        listener = location -> {
//            Log.d("GPS_TIME", Long.toString(location.getTime()));
//            Log.d("GPS_LON", Double.toString(location.getLongitude()));
//            Log.d("GPS_LAT", Double.toString(location.getLatitude()));
//            Log.d("GPS_ACC", Float.toString(location.getAccuracy()));
            GPSLocationLiveData.this.setValue(location);
        };
    }

    /**
     * set the minimum time between updates
     * @param minTimeMs minimum time between two updates in milliseconds
     */
    public void setMinTimeMs(long minTimeMs) {
        this.minTimeMs = minTimeMs;
    }

    /**
     * set the minimum distance between updates
     * @param minDistanceM minimum distance between two updates in meters
     */
    public void setMinDistanceM(float minDistanceM) {
        this.minDistanceM = minDistanceM;
    }

    /** @return true if the app is granted location service permission */
    public boolean isPermissionGranted() {
        return !(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * called when an observer is attached, the livedata will start updating its location regularly
     * by sending requests to GPS location tracker
     */
    @Override
    protected void onActive() {
        super.onActive();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("GPS", "GPS location permission has not been obtained before observe() call!");
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTimeMs, minDistanceM, listener);
        }
    }

    /** called when the last active observer becomes inactive or is destroyed */
    @Override
    protected void onInactive() {
        super.onInactive();
        locationManager.removeUpdates(listener);
    }
}
