package com.qrcode_quest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

public class MockLocationManager {

    public static class LocationRequestSetting {
        public Location lastKnownLocation;
        public String provider;
        public long minTimeMs;
        public float minDistanceM;
        public LocationRequestSetting(String provider, long minTimeMs, float minDistanceM) {
            this.provider = provider;
            this.minTimeMs = minTimeMs;
            this.minDistanceM = minDistanceM;
            this.lastKnownLocation = null;
        }
    }

    static public LocationManager createMockLocationManager(LifecycleOwner lifecycleOwner,
                                                            LiveData<Location> actualLocation) {
        LocationManager manager = mock(LocationManager.class);

        HashMap<LocationListener, LocationRequestSetting> requests = new HashMap<>();

        doAnswer(invocation -> {
            String provider = invocation.getArgument(0);
            Long time = invocation.getArgument(1);
            Float distance = invocation.getArgument(2);
            LocationListener listener = invocation.getArgument(3);

            requests.put(listener, new LocationRequestSetting(provider, time, distance));

            return null;
        }).when(manager).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(LocationListener.class));

        doAnswer(invocation -> {
            LocationListener listenerToRemove = invocation.getArgument(0);
            requests.remove(listenerToRemove);
            return null;
        }).when(manager).removeUpdates(any(LocationListener.class));

        actualLocation.observe(lifecycleOwner, location -> {
            // broadcast change to all location listeners
            for(LocationListener listener: requests.keySet()) {
                LocationRequestSetting setting = requests.get(listener);
                assert setting != null;
                String provider = location.getProvider();
                if (provider.equals(setting.provider)) {
                    Location lastKnownLocation = setting.lastKnownLocation;
                    if (lastKnownLocation == null) {
                        // first time set location
                        setting.lastKnownLocation = location;
                        listener.onLocationChanged(location);
                    } else if (lastKnownLocation.distanceTo(location) >= setting.minDistanceM
                            && (location.getTime() - lastKnownLocation.getTime()) >= setting.minTimeMs) {
                        // update location
                        setting.lastKnownLocation = location;
                        listener.onLocationChanged(location);
                    }
                    // otherwise ignore the update
                }
            }
        });

        return manager;
    }
}
