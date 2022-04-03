package com.qrcode_quest.userStoriesTests;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasXPath;

import android.util.Log;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.qrcode_quest.EspressoHelper;
import com.qrcode_quest.MainActivity;
import com.qrcode_quest.MockInstances;
import com.qrcode_quest.R;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.matchers.TextInputLayoutMatcher;
import com.qrcode_quest.ui.login.LoginActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * Tests user stories for Player Profile functionality.
 *
 * @author egwhite
 * @version 1.0
 */
public class PlayerProfileTests {

    //US 04.0X.01
    //As a player, I do not want to log into my application using a username and password as my device can identify me.
    @Test
    public void deviceIDTest(){

        QRCodeQuestApp app = ApplicationProvider.getApplicationContext();
        app.resetContainer();

        PlayerAccount testPlayer = new PlayerAccount("TestUsername",
                "test@email.com", "123-456-7890", false, false);
        String deviceID = "test";

        AppContainer container = app.getContainer();
        container.setDb(MockInstances.createSinglePlayerDb(testPlayer, deviceID));
        container.setStorage(MockInstances.createEmptyPhotoStorage());
        container.setPrivateDevicePrefs(
                MockInstances.createRegisteredPreferences(testPlayer.getUsername(), deviceID));


        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(isRoot()).perform(EspressoHelper.waitFor(1000));
        onView(withId(R.id.text_home)).check(matches(TextInputLayoutMatcher.hasNoError())); //check login was successful and are on MainActivity

    }
}
