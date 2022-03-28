package com.qrcode_quest.entities;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.testing.TestLifecycleOwner;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.qrcode_quest.MainActivity;
import com.qrcode_quest.MockInstances;
import com.qrcode_quest.MockLocationManager;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class GPSLocationLiveDataTest {
    final String GPSProvider = LocationManager.GPS_PROVIDER;
    MutableLiveData<Location> actualLocation;

    // StackOverflow, by donturner and Amin Keshavarzian
    // url: https://stackoverflow.com/questions/50403128/how-to-grant-permissions-to-android-instrumented-tests
    @Rule
    public GrantPermissionRule fineLocRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule coarseLocRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);
    public ActivityScenarioRule<MainActivity> rule;

    @Rule
    public ActivityScenarioRule<MainActivity> setupRule() {
        // get the application object so we can provide mock db and other dependencies to it
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        AppContainer container = app.getContainer();

        rule = new ActivityScenarioRule<>(MainActivity.class);
        return rule;
    }

    private void setMockLocation(Geolocation l, long time) {
        Location mockLocation = new Location(GPSProvider);
        mockLocation.setLatitude(l.getLatitude());
        mockLocation.setLongitude(l.getLongitude());
        // timestamp of geolocation data
        mockLocation.setTime(time);
        mockLocation.setElapsedRealtimeNanos(time * 1000000);
        mockLocation.setAccuracy((float) 0.1);  // we don't care about accuracy, just a placeholder
        actualLocation.setValue(mockLocation);
    }

    @Before
    public void setupLocationLiveData() {
        actualLocation = new MutableLiveData<>();
    }

    @Test
    public void testObserveLocation() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            @Override
            public void perform(MainActivity activity) {
                Context context = activity.getApplicationContext();
                TestLifecycleOwner lifecycleOwner = new TestLifecycleOwner();

                LocationManager locationManager = MockLocationManager.createMockLocationManager(
                        lifecycleOwner, actualLocation);
                GPSLocationLiveData locationLiveData = new GPSLocationLiveData(
                        context, locationManager);

                Geolocation path[] = {
                        new Geolocation(1.0, 1.0),
                        new Geolocation(2.0, 2.0),
                        new Geolocation(3.0, 3.0),
                };
                double results[] = {0.0, 0.0};
                Observer<Geolocation> latObserver = new Observer<Geolocation>() {
                    @Override
                    public void onChanged(Geolocation location) {
                        if (location != null)
                            results[0] = location.getLatitude();
                    }
                };
                Observer<Geolocation> lonObserver = new Observer<Geolocation>() {
                    @Override
                    public void onChanged(Geolocation location) {
                        if (location != null)
                            results[1] = location.getLongitude();
                    }
                };

                locationLiveData.observe(lifecycleOwner, latObserver);
                setMockLocation(path[0], 1);
                assertEquals(1.0, results[0], 0.0);
                assertEquals(0.0, results[1], 0.0);

                locationLiveData.observe(lifecycleOwner, lonObserver);
                setMockLocation(path[1], 10000);  // make sure at least 5000 ms has passed
                assertEquals(2.0, results[0], 0.0);
                assertEquals(2.0, results[1], 0.0);

                locationLiveData.removeObserver(latObserver);
                setMockLocation(path[2], 20000);
                assertEquals(2.0, results[0], 0.0);
                assertEquals(3.0, results[1], 0.0);

                locationLiveData.removeObserver(lonObserver);
                setMockLocation(path[1], 30000);
                assertEquals(2.0, results[0], 0.0);
                assertEquals(3.0, results[1], 0.0);

                // ignore updates when time or distance is not satisfied
            }
        });
    }
}
