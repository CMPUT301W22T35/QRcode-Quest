package com.qrcode_quest.ui.map;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


/**
 * A ViewModel for MapFragment to store previously recorded location
 *
 * @author ageolleg
 * @version 1.1
 */
public class MapViewModel extends ViewModel {
    MutableLiveData<Location> lastLocation;

    public MutableLiveData<Location> getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(MutableLiveData<Location> currentLocation) {
        this.lastLocation = currentLocation;
    }
}
