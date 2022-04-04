package com.qrcode_quest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.Comment;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;
import com.qrcode_quest.ui.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String CLASS_TAG = "MainActivityTest";

    // Some constant sample data for testing
    private static final Geolocation TEST_GEOLOC = new Geolocation(37.422131, -122.084801);
    private static final String DEVICE_ID = "testid";
    private static final PlayerAccount localPlayer = new PlayerAccount(
            "testuser", "email@test.ca", "780-123-45678");


    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    );

    /**
     * Loads and populates test data into mocked database objects
     *
     * 4 PlayerAccounts:<br>
     * {localUser}<br>
     * test_0 - "" - ""<br>
     * test_1 - "" - ""<br>
     * test_2 - "" - ""<br>
     * <br>
     * 4 QR Shots:<br>
     * {localUser}, test_qr_0<br>
     * test_0, test_qr_0<br>
     * test_0, test_qr_1<br>
     * test_1, test_qr_0<br>
     * <br>
     * 2 Comments:<br>
     * hash:test_qr_0, user:test_0, content:test comment_0
     * hash:test_qr_0, user:test_1, content:test comment_1
     *
     * @param asOwner Initializes the program with the local user's owner flag set to 'asOwner'
     */
    private void initAndPopulateStorage(boolean asOwner){
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();
        AppContainer container = app.getContainer();

        ArrayList<PlayerAccount> players = new ArrayList<>();
        players.add( new PlayerAccount("test_0", "",""));
        players.add( new PlayerAccount("test_1", "",""));
        players.add( new PlayerAccount("test_2", "",""));

        ArrayList<QRShot> shots = new ArrayList<>();
        ArrayList<Comment> comments = new ArrayList<>();

        try{
            String qr_0_hash = new RawQRCode("test_qr_0").getQRHash();
            String qr_1_hash = new RawQRCode("test_qr_1").getQRHash();

            shots.add( new QRShot(localPlayer.getUsername(), qr_0_hash));
            shots.add( new QRShot("test_0", qr_0_hash));
            shots.add( new QRShot("test_1", qr_0_hash));

            shots.add( new QRShot("test_0", qr_1_hash, null, TEST_GEOLOC));

            comments.add(new Comment("test_0", "test comment_0", qr_0_hash));
            comments.add(new Comment("test_1", "test comment_1", qr_0_hash));
        } catch (Exception e){
            Log.e("QRFragmentViewTest", "Failed to load QR Shots " + e.getMessage());
            assert false;
        }

        localPlayer.setOwner(asOwner);
        MockStorageBundle dbBundle =
                MockInstances.createPopulatedDb(players, shots, comments, localPlayer, DEVICE_ID);

        container.setDb(dbBundle.db);
        container.setStorage(dbBundle.photoStorage);
        container.setPrivateDevicePrefs(
                MockInstances.createRegisteredPreferences(localPlayer.getUsername(), DEVICE_ID));
    }


    /** Tests that the leaderboard calculates rank correctly and displays users */
    @Test
    public void testLeaderboard(){
        initAndPopulateStorage(false);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        // Navigate to the leaderboard
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());

        // Check relevant data has been displayed
        onView(withText(localPlayer.getUsername())).check(matches(isDisplayed()));
        onView(withText("test_0")).check(matches(isDisplayed()));
        onView(withText("0")).check(matches(isDisplayed()));
        try {
            String qr_0_hash = new RawQRCode("test_qr_0").getQRHash();
            int score = RawQRCode.getScoreFromHash(qr_0_hash);
            onView(withText("" + score)).check(matches(isDisplayed()));
        } catch (Exception ignored) {}

        // Test standings
        onView(withId(R.id.playerlist_totalcaptures)).check(matches(withText("2nd")));
        onView(withId(R.id.playerlist_totalscore)).check(matches(withText("2nd")));
        onView(withId(R.id.playerlist_bestcapture)).check(matches(withText("1st")));

        scenario.close();
    }

    /** Tests a QRShot with the minimum required fields */
    @Test
    public void testBareBonesQRView() {
        initAndPopulateStorage(false);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        assert !localPlayer.getUsername().equals("test_0");

        // Navigate to the desired code
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText("test_0")).perform(click());

        try {
            RawQRCode bareCode = new RawQRCode("test_qr_0");
            String hash = bareCode.getQRHash();
            String hashName = hash.substring(hash.length() - 5);

            onView(withText(hashName)).perform(click());

            // Check all UI elements are working
            onView(withId(R.id.qrview_name)).check(matches(withText(hashName)));
            onView(withId(R.id.qrview_score)).check(matches(withText(""+bareCode.getScore())));
            onView(withId(R.id.qrview_other_scans)).check(matches(withText(""+3)));

            // Ensure views are hidden correctly
            onView(withId(R.id.qrview_photo)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.qrview_geoloc)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.qrview_delete_button)).check(matches(withEffectiveVisibility(Visibility.GONE)));

            // Test the "other players" list
            onView(withId(R.id.qrview_other_scans_button)).perform(scrollTo(), click());
            onView(withId(R.id.qrview_playerlist)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

            // Check the list has the relevant players
            onView(withText("test_0")).check(matches(isDisplayed()));
            onView(withText("test_1")).check(matches(isDisplayed()));
            onView(withText(localPlayer.getUsername())).check(matches(isDisplayed()));

        } catch (Exception e) {
            Log.e(CLASS_TAG, e.getClass() + ": " + e.getMessage());
            assert false;
        }
        scenario.close();
    }

    /** Tests the QR view with a QRShot that has a Geolocation */
    @Test
    public void testQRViewWithGeoLoc(){
        initAndPopulateStorage(false);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        // Navigate to the desired code
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText("test_0")).perform(click());

        try {
            RawQRCode codeWithGeo = new RawQRCode("test_qr_1");
            String hash = codeWithGeo.getQRHash();
            String hashName = hash.substring(hash.length() - 5);
            onView(withText(hashName)).perform(click());
        } catch (Exception e) {
            Log.e(CLASS_TAG, e.getClass() + ": " + e.getMessage());
            assert false;
        }

        // Check the geolocation container was displayed correctly
        onView(withId(R.id.qrview_geoloc_container)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(R.id.qrview_geoloc)).check(matches(withText(TEST_GEOLOC.toString())));

        scenario.close();
    }

    /** Tests deleting an owned QR shot and ensuring players cannot be deleted by non-owners */
    @Test
    public void testCanDeleteOwnedQRNotPlayer() {
        initAndPopulateStorage(false);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        assert !localPlayer.isOwner();

        // Navigate to the desired code
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText(localPlayer.getUsername())).perform(click());

        // Check cannot delete a player
        onView(withId(R.id.player_qrlist_deleteplayer_button
         )).check(matches(withEffectiveVisibility(Visibility.GONE)));

        try {
            RawQRCode bareCode = new RawQRCode("test_qr_0");
            String hash = bareCode.getQRHash();
            String hashName = hash.substring(hash.length() - 5);
            onView(withText(hashName)).perform(click());
        } catch (Exception e) {
            Log.e(CLASS_TAG, e.getClass() + ": " + e.getMessage());
            assert false;
        }

        // Check delete owned capture button visible
        onView(withId(R.id.qrview_delete_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Check deletion functionality
        onView(withId(R.id.qrview_delete_button)).perform(click());
        scenario.close();
    }

    /** Tests an owner can delete a player or a QR code */
    @Test
    public void testOwnerCanDeleteAny() {
        initAndPopulateStorage(true);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        assert localPlayer.isOwner();
        assert !localPlayer.getUsername().equals("test_0");

        // Navigate to the desired code
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText("test_0")).perform(click());

        // Ensure delete player is visible
        onView(withId(R.id.player_qrlist_deleteplayer_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        try {
            RawQRCode bareCode = new RawQRCode("test_qr_0");
            String hash = bareCode.getQRHash();
            String hashName = hash.substring(hash.length() - 5);
            onView(withText(hashName)).perform(click());

            // Delete a qr shot
            onView(withId(R.id.qrview_delete_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.qrview_delete_button)).perform(click());
            onView(isRoot()).perform(EspressoHelper.waitFor(250));
            onView(withText(hashName)).check(doesNotExist());

            // Delete a player
            onView(withId(R.id.player_qrlist_deleteplayer_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.player_qrlist_deleteplayer_button)).perform(click());
            onView(isRoot()).perform(EspressoHelper.waitFor(250));
            onView(withText("test_0")).check(doesNotExist());
        } catch (Exception e) {
            Log.e(CLASS_TAG, e.getClass() + ": " + e.getMessage());
            assert false;
        }
        scenario.close();
    }


    /** Test if comments are displayed and added correctly. */
    @Test
    public void testComments(){
        initAndPopulateStorage(false);
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        assert !localPlayer.isOwner();

        // Navigate to the desired code
        onView(isRoot()).perform(EspressoHelper.waitFor(250));
        onView(withId(R.id.navigation_leaderboard)).perform(click());
        onView(withText(localPlayer.getUsername())).perform(click());
        try {
            RawQRCode bareCode = new RawQRCode("test_qr_0");
            String hash = bareCode.getQRHash();
            String hashName = hash.substring(hash.length() - 5);
            onView(withText(hashName)).perform(click());
        } catch (Exception e) {
            Log.e(CLASS_TAG, e.getClass() + ": " + e.getMessage());
            assert false;
        }
        onView(withId(R.id.qrview_comments_button)).perform(scrollTo(), click());

        // Test comments are present
        onView(withText("test_0")).check(matches(isDisplayed()));
        onView(withText("test comment_0")).check(matches(isDisplayed()));
        onView(withText("test_1")).check(matches(isDisplayed()));
        onView(withText("test comment_1")).check(matches(isDisplayed()));

        // Post a new comment
        onView(withId(R.id.comments_input)).perform(typeText("new comment"));
        onView(withId(R.id.comments_post_button)).perform(click());

        // Wait for the update
        onView(isRoot()).perform(EspressoHelper.waitFor(250));

        // Check it appeared
        onView(withText(localPlayer.getUsername())).check(matches(isDisplayed()));
        onView(withText("new comment")).check(matches(isDisplayed()));

        scenario.close();
    }



}
