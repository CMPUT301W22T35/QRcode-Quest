package com.qrcode_quest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.GPSLocationLiveData;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class MainActivityMapTest {
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE);

    public ActivityScenarioRule<MainActivity> rule;

    @Rule
    public ActivityScenarioRule<MainActivity> setupRule() {
        // get the application object so we can provide mock db and other dependencies to it
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        PlayerAccount testPlayer = new PlayerAccount(
                "testPlayerName",
                "testplayer@gmail.com",
                "123-456-7890", false, true);

        ArrayList<QRShot> testQRShots = new ArrayList<>();
        QRShot testQRShot1 = new QRShot(
                "testPlayerName",
                "5694d08a2e53ffcae0c3103e5ad6f6076abd960eb1f8a56577040bc1028f702b",
                null, new Geolocation(53.526221, -113.520771));
        testQRShots.add(testQRShot1);
        QRShot testQRShot2 = new QRShot(
                "testPlayerName",
                "3fc9b689459d738f8c88a3a48aa9e33542016b7a4052e001aaa536fca74813cb",
                null, new Geolocation(53.523402, -113.52782));
        testQRShots.add(testQRShot2);
        String deviceID = "";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createPlayersQRShotsDb(testQRShots, testPlayer, deviceID));

        rule = new ActivityScenarioRule<>(MainActivity.class);
        return rule;
    }

    /**
     * Tests whether the map gets properly displayed
     */
    @Test
    public void checkMapDisplayed() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        onView(withId(R.id.home_map_button)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(5000));

        MapView[] mapViews = new MapView[1];
        scenario.onActivity(activity -> {
                    mapViews[0] = activity.findViewById(R.id.mapView); // Get the map view
        });

        assertTrue(mapViews[0].canZoomIn());
        assertTrue(mapViews[0].canZoomOut());
        assertTrue(mapViews[0].isTilesScaledToDpi());

        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        onView(withId(R.id.mapListActionButton)).check(matches(isDisplayed()));
        onView(withId(R.id.mapListActionButton)).perform(click());
        onView(withId(R.id.map_list)).check(matches(isDisplayed()));
    }

    /**
     * Tests for correct display of player/device location
     */
    @Test
    public void checkMapCurrentLocation() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        onView(withId(R.id.home_map_button)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(5000));

        double latLon[] = {0.0, 0.0};
        MapView[] mapViews = new MapView[1];
        scenario.onActivity(activity -> {
            mapViews[0] = activity.findViewById(R.id.mapView); // Get the map view

            // Get the device's current location
            Context context = activity.getApplicationContext();
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            GPSLocationLiveData locationLiveData = new GPSLocationLiveData(context, locationManager);
            locationLiveData.observe(activity, location -> {
                    if (location != null){
                        latLon[0] = location.getLatitude();
                        latLon[1] = location.getLongitude();
                    }
            });
        });

        // Check device's current location matches with the marked location on the map
        onView(isRoot()).perform(EspressoHelper.waitFor(5000));
        Marker playerMarker = (Marker) mapViews[0].getOverlays().get(1);
        assertEquals(latLon[0], playerMarker.getPosition().getLatitude(),0.002);
        assertEquals(latLon[1], playerMarker.getPosition().getLongitude(), 0.002);

        // Check device's current location matches with the Map's center location/current location
        assertEquals(latLon[0], mapViews[0].getMapCenter().getLatitude(),0.002);
        assertEquals(latLon[1], mapViews[0].getMapCenter().getLongitude(), 0.002);
    }

    /**
     * Tests for correct display of nearby QR codes (Map and List)
     */
    @Test
    public void checkMapNearbyQRCodes() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        onView(withId(R.id.home_map_button)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(5000));

        MapView[] mapViews = new MapView[1];
        scenario.onActivity(activity -> {
            mapViews[0] = activity.findViewById(R.id.mapView);
        });

        // Check for 2 markers (nearby locations)
        mapViews[0].getOverlays().remove(0); // remove scale bar overlay first
        mapViews[0].getOverlays().remove(0); // remove current location marker
        assertFalse(mapViews[0].getOverlays().isEmpty());
        assertEquals(2, mapViews[0].getOverlays().size());

        // Check location of nearby qr code markers
        Marker qrMarker1 = (Marker) mapViews[0].getOverlays().get(0);
        Marker qrMarker2 = (Marker) mapViews[0].getOverlays().get(1);
        assertEquals(53.52622, qrMarker1.getPosition().getLatitude(),0.002);
        assertEquals(-113.52782, qrMarker2.getPosition().getLongitude(), 0.002);

        // Check correct displays in MapList
        onView(withId(R.id.mapListActionButton)).perform(click());
        onView(withText("Latitude: 53.52622 Longitude: -113.52077")).check(matches(isDisplayed()));
        onView(withText("Latitude: 53.52340 Longitude: -113.52782")).check(matches(isDisplayed()));
    }

    /**
     * Tests the back buttons
     */
    @Test
    public void checkMapBackButtons(){
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        onView(withId(R.id.home_map_button)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(5000));

        onView(withContentDescription("Navigate up")).perform(click());
        onView(withId(R.id.mapView)).check(doesNotExist());

        onView(withId(R.id.mapListActionButton)).check(doesNotExist());

        onView(withId(R.id.home_map_button)).perform(click());
        onView(withId(R.id.mapListActionButton)).perform(click());

        onView(withContentDescription("Navigate up")).perform(click());
        onView(withContentDescription("Navigate up")).perform(click());
        onView(withId(R.id.mapView)).check(doesNotExist());
        onView(withId(R.id.mapListActionButton)).check(doesNotExist());
    }

    @After
    public void tearDown(){
        rule.getScenario().close();
    }


}
