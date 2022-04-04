package com.qrcode_quest;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.hamcrest.CoreMatchers.is;

import android.Manifest;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.matchers.TextInputLayoutMatcher;
import com.qrcode_quest.ui.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the LoginActivity functionality.
 *
 * @author jdumouch
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA
    );

    /** Tests the case where the user has account data that does not exist on the database. */
    @Test
    public void testInvalidUser() {
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        PlayerAccount testPlayer = new PlayerAccount("TestUsername",
                "test@email.com", "123-456-7890", false, false);
        String deviceID = "testid";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createEmptyDb());
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(
                MockInstances.createRegisteredPreferences(testPlayer.getUsername(), deviceID));
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(isRoot()).perform(EspressoHelper.waitFor(1000));
        onView(withId(R.id.login_fragment_container)).check(matches(isDisplayed()));

        scenario.close();
    }

    /** Tests the case where a user already registered and has account data on their device. */
    @Test
    public void testValidUser() {
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        PlayerAccount testPlayer = new PlayerAccount("TestUsername",
                "test@email.com", "123-456-7890", false, false);
        String deviceID = "testid";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createSinglePlayerDb(testPlayer, deviceID));
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(
                MockInstances.createRegisteredPreferences(testPlayer.getUsername(), deviceID));

        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(isRoot()).perform(EspressoHelper.waitFor(1000));
        assertThat(scenario.getState(), is(Lifecycle.State.DESTROYED));
        scenario.close();
    }

    /**
     * Tests the SignupFragment for correct registration functionality.
     * This runs through the text validation and finally the registration.
     */
    @Test
    public void testRegistration() {
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        PlayerAccount testPlayer = new PlayerAccount("TestUsername",
                "test@email.com", "123-456-7890", false, false);
        String deviceID = "testid";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createSinglePlayerDb(testPlayer, deviceID));
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(
                MockInstances.createEmptySharedPreferences());
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        /* No info, pressed register */
        onView(withId(R.id.login_signup_register_button)).perform(click());
            // Check the 'username required' error
        onView(withId(R.id.login_signup_username_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("Username is required")));
            // Ensure the email layout didn't error
        onView(withId(R.id.login_signup_email_layout)).check(
                matches(TextInputLayoutMatcher.hasNoError()));

        /* Invalid username, no email */
        onView(withId(R.id.login_signup_username)).perform(
                click(),
                typeText("user name")
        );
        closeSoftKeyboard();
            // Press register again
        onView(withId(R.id.login_signup_register_button)).perform(click());

            // Check the 'invalid username' error
        onView(withId(R.id.login_signup_username_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("No whitespace allowed")));
            // Ensure the email layout didn't error
        onView(withId(R.id.login_signup_email_layout)).check(
                matches(TextInputLayoutMatcher.hasNoError()));


        /* Invalid email, Invalid username */
        onView(withId(R.id.login_signup_email)).perform(
                click(),
                typeText("bad@email")
        );
        closeSoftKeyboard();
            // Press register again
        onView(withId(R.id.login_signup_register_button)).perform(click());
            // Check the 'invalid username' error
        onView(withId(R.id.login_signup_username_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("No whitespace allowed")));
            // Ensure the email layout didn't error
        onView(withId(R.id.login_signup_email_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("Invalid email address")));


        /* Invalid email, taken username */
        onView(withId(R.id.login_signup_username)).perform(
                click(),
                replaceText("TestUsername")
        );
        closeSoftKeyboard();
            // Press register again
        onView(withId(R.id.login_signup_register_button)).perform(click());

        // Check the 'invalid username' error
        onView(withId(R.id.login_signup_username_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("Username taken")));
            // Ensure the email layout didn't error
        onView(withId(R.id.login_signup_email_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("Invalid email address")));


        /* valid email, taken username */
        onView(withId(R.id.login_signup_email)).perform(
                click(),
                replaceText("test@email.com")
        );
        closeSoftKeyboard();
            // Press register again
        onView(withId(R.id.login_signup_register_button)).perform(click());
            // Check the 'invalid username' error
        onView(withId(R.id.login_signup_username_layout)).check(
                matches(TextInputLayoutMatcher.hasErrorText("Username taken")));
            // Ensure the email layout didn't error
        onView(withId(R.id.login_signup_email_layout)).check(
                matches(TextInputLayoutMatcher.hasNoError()));


        /* valid email, valid username */
        onView(withId(R.id.login_signup_username)).perform(
                click(),
                replaceText("username")
        );
        closeSoftKeyboard();
            // Press register again
        onView(withId(R.id.login_signup_register_button)).perform(click());

        // Wait for auth and then check the activity is dead (signed in)
        onView(isRoot()).perform(EspressoHelper.waitFor(500));
        assertThat(scenario.getState(), is(Lifecycle.State.DESTROYED));
        scenario.close();
    }

    @Test
    public void testScanSignIn(){
        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createEmptyDb());
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(
                MockInstances.createEmptySharedPreferences());
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_signup_scan_button)).perform(click());
        onView(isRoot()).perform(EspressoHelper.waitFor(1000));
        onView(withId(R.id.viewfinder_view)).check(matches(isDisplayed()));
        scenario.close();
    }
}
