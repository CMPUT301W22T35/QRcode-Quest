package com.qrcode_quest.ui.map;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


/**
 * A ViewModel for MapFragment to store previously recorded location
 *
 * @author ageolleg
 * @version 1.0
 */
public class MapViewModel extends ViewModel {
    private MutableLiveData<Location> lastLocation;

    /** the the location live data of the last location
     * @return location live data of last location
     */
    public MutableLiveData<Location> getLastLocation() {
        return lastLocation;
    }

    /**
     * set the location live data to the given location
     * @param currentLocation current location's livedata object
     */
    public void setLastLocation(MutableLiveData<Location> currentLocation) {
        this.lastLocation = currentLocation;
    }
}
